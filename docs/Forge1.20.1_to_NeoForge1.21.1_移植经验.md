# Forge 1.20.1 → NeoForge 1.21.1 移植经验（实战踩坑总结）

> 来源：四个模组（累计 10 万行以上 Java、900+ 数据文件、3 个结构模板）从 Forge 1.20.1 迁移到 NeoForge 1.21.1 的完整过程。
> **所有条目都是实际踩过并定位过根因的**，不是推测。建议**从上往下读一遍再动手**，能省掉大量来回。
> 环境：MC 1.21.1 · NeoForge 21.1.176 / 21.1.220（编译）· ModDevGradle 2.0.91+ · Java 21 · Gradle 8.14.2
>
> **v2 更新**：新增 §11 无窗口验证方法论、§12 实体/伤害/死亡语义、§13 结构 NBT、§14 可选依赖隔离、
> §15 mixin 实战、§16 原版硬编码与槽位、§17 发布与 CI；§3/§4/§8/§9 补充了 1.21 的新变化；
> 速查表扩到 30 条。加 **🆕** 的条目是 v2 新增。
>
> 文中示例会出现具体模组的类名/物品名（`epca`、`eej`、`PlayerAnimator`、`ModEffects`…），
> 它们只是"当时那个模组"的实例，**换成你手上的目标模组即可**，结论与手法不变。

---

## 这份文档怎么用（给接手移植的会话）

> 这是一份**作业手册**，不是随笔。目标读者：第一次接触目标模组、**不能开客户端**、要独立完成移植的会话。
> 按"模式"选入口，不要从头乱翻。

| 你的处境 | 入口 | 然后 |
|---|---|---|
| 从零开始移植一个 Forge 1.20.1 模组 | 先读 §0（七条铁律），再按 §19 的 14 步走 | 每一步用对应小节里的**验证命令**收尾，不要"改完就算" |
| 已经能编译，但**功能不对** | §18 症状速查表 | 命中条目 → 跳到对应小节，按里面的"根因 + 修法 + 验证"三步走 |
| 只想**验收**别人的成果 | §21 验收清单 | 逐项勾选，每项都要有"命令 + 输出"作为证据 |
| 要**交接**给下一个会话 | §22 交接提示词模板 | 把模板 + 目标模组信息一起发过去 |

### 三条硬性约定（违反会浪费大量时间）

1. **不要开客户端验证**（§11）。能 `runServer` + RCON + 临时命令验的，一律不要开窗口——
   开窗口既不可脚本化，也会漏掉"存档 → 读档"这类只有服务端才暴露的路径。
2. **每一处修改都要留下可复现的证据**：一条命令 + 一段输出（`Done (x.xxxs)` / `Test passed` /
   `Successfully filled N block(s)` / `0 ERROR`）。没有证据的"应该没问题"一律不算完成。
3. **遇到"静默失效"先按优先级怀疑**：`@Override` 死方法（§3）→ `Holder` 身份比较（§3.6）→
   数据文件没搬/目录名错（§1§2）→ 数据格式静默失效（§4.5）→ 实体语义变化（§12）。

---

## 0. 先记住这七条（最省时间的七条）

1. **1.21 把数据包目录从复数改成了单数** —— 不改就**静默失效**，不报错。这是最容易漏、影响面最大的一条。
2. **1.20.1 的方法签名在 1.21.1 上可能不再构成覆盖** —— 方法体永远不执行，也是静默的。
3. **拿 1.20.1 参考树 diff** 是定位迁移 bug 最高效的手段，90% 的问题靠它。
4. **所有签名/行为用 `javap` 或源码 jar 核实，不要凭记忆** —— "凭记忆"在本项目里导致过多次假修复。
5. **客户端和服务端的 mod 集合必须一致** —— 否则 `BuiltInRegistries` 的注册顺序不同，网络包里的 id 会错位，表现为"进存档断线"。
6. 🆕 **1.21 对"空 ItemStack"零容忍** —— `ItemStack#save` / `ItemStack.STREAM_CODEC` 遇到空栈**抛异常**（1.20.1 只是写个空 compound）。任何"可能为空"的栈都要在序列化前兜住。
7. 🆕 **不要在客户端开会话验证** —— 全部用 `runServer` + RCON + 临时调试命令做无窗口验证（§11），否则你会在"打不开游戏 / 开了游戏也看不到"之间反复空转。

---

## 1. 数据包目录改名（最高优先级 ★★★★★）

### 1.1 改了什么

| 1.20.1（复数） | 1.21.1（单数） |
|---|---|
| `data/<ns>/tags/blocks/…` | `data/<ns>/tags/block/…` |
| `data/<ns>/tags/items/…` | `data/<ns>/tags/item/…` |
| `data/<ns>/tags/fluids/…` | `data/<ns>/tags/fluid/…` |
| `data/<ns>/tags/entity_types/…` | `data/<ns>/tags/entity_type/…` |
| `data/<ns>/recipes/…` | `data/<ns>/recipe/…` |
| `data/<ns>/loot_tables/…` | `data/<ns>/loot_table/…` |
| `data/<ns>/advancements/…` | `data/<ns>/advancement/…` |
| `data/<ns>/predicates/…` | `data/<ns>/predicate/…` |
| `data/<ns>/item_modifiers/…` | `data/<ns>/item_modifier/…` |
| `data/<ns>/functions/…` | `data/<ns>/function/…` |
| `data/<ns>/structures/…` | `data/<ns>/structure/…` |

### 1.2 三条"别改"的（改了会把好的改坏）

- **`loot_modifiers` 的复数是正确的** —— NeoForge 自己就用 `data/<ns>/loot_modifiers/global_loot_modifiers.json`，**不要**改成 `loot_modifier`。
- **`tags` 父目录名本来就是对的** —— 只有它的**子目录**要改单数。
- **`advancement/recipes/`** 是原版也有的**合法子目录**（进度分类），**不要**改名。

### 1.3 怎么验证（两种，都很快）

```powershell
# ① 权威依据：从原版资源 jar 里列 data/minecraft 的实际一级目录
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead('<工程>/build/moddev/artifacts/neoforge-21.1.220-client-extra-aka-minecraft-resources.jar')
$z.Entries | Where-Object { $_.FullName -match '^data/minecraft/[^/]+/$' } | ForEach-Object { $_.FullName }
$z.Dispose()
# 1.21.1 里只有 recipe / loot_table / advancement / structure / tags / worldgen …  复数一个都不存在

# ② 出包后确认无复数残留
#    期望结果为空
jar tf build/libs/<你的jar> | findstr /R "tags/blocks tags/items recipes/ loot_tables/ advancements/"
```

> ⚠️ 加载器侧证据：`Registries.tagsDirPath` = `"tags/" + prefixNamespace(key)`，`TagLoader` **只用一个目录、没有复数回退**。
> 所以复数目录里的文件是**纯死数据**：不报错、不警告、就是没有。

### 1.4 🆕 结构模板（`.nbt`）也是数据包内容

`data/<ns>/structure/*.nbt` 同样走单数目录。**它不会出现在 `runData` 产物里**，也不会报错——
如果目录名写错，结构就是"注册不到"，`/place structure` 会直接说找不到。

### 1.5 🆕 `.gitattributes` 必须把二进制数据标成 binary

Windows 上 `core.autocrlf=true` 时，git 会把 `.nbt` 当文本做 CRLF 转换 —— **结构文件会被逐字节改坏**
（NBT 的字符串是 `[u16 长度][原始字节]`，插进去一个 `\r` 长度就对不上）。表现是游戏里结构加载失败或解析异常。

```
*.jar binary
*.nbt binary
*.png binary
*.ogg binary
*.zip binary
*.glb binary
*.class binary
```

改完 `git add --renormalize .`，再用 `git hash-object --no-filters <file>` 和
`git rev-parse HEAD:<path>` 对比，确认远程 blob 与本地**原始字节**一致。

---

## 2. 标签命名空间：`forge:` → `c:`（但别盲改）★

- NeoForge 1.21.1 **没有 `forge:` 标签命名空间**了，官方约定是 **`c:`**。
- ⚠️ **但不要机械地把 `forge:xxx` 全改成 `c:xxx`**：`c:` 只覆盖官方约定的一部分（例如 `c:tools/{wrench,shear,bow,pickaxe…}` 有，但 **`c:tools/hammers`、`c:tools/crowbars`、`c:tools/saws` 这些根本没有**）。
- 在别人的命名空间里**发明**标签，既不是约定、又会破坏仍读 `forge:` 的第三方模组。

### 2.1 一个反直觉但很重要的事实

> **`data/forge/tags/item/xxx.json` 不是死文件。**
> 标签命名空间是**任意的** —— 它**会真的注册出 `forge:xxx` 并生效**。
> 只有"代码里硬编码了某个具体路径"的情况（如 `LootModifierManager` 只读 `neoforge:loot_modifiers/global_loot_modifiers`）才是真死。

所以处理 `forge:` 标签前先问：**有没有别的模组/我们自己的代码在读它？** 有 ⇒ 保留；没有 ⇒ 才考虑清理。

### 2.2 处理原则

1. 先 **grep 代码侧所有 `TagKey.create(...)` / `ModTags.*`**，列出真正被读取的标签。
2. 再对账"文件是否存在、内容是否非空"——**空标签是静默的**（配方永远匹配不上）。
3. `forge:` → `c:` **只做有官方对应物的**；没有对应物的**保留原样**并记录。

### 2.3 🆕 最有效的一招：拿"官方发布包"对账标签

移植工程最容易漏的不是改错，而是**根本没搬过来**。做法：

```powershell
# 从 Modrinth/CurseForge 下载官方 1.20.1 发布 jar，逐条比对 data/ 与 assets/ 的文件清单
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead('官方发布.jar')
$z.Entries | Where-Object { $_.FullName -like 'data/*' } | ForEach-Object { $_.FullName }
```

本项目的真实案例：上游发布包里有两个标签（`forge:enchanting_fuels`、`forge:infested_flesh`）在移植工程里**压根没提交**。
其中 `c:infested_flesh` 是某个配方唯一的材料来源 ⇒ **该配方永远不会匹配**（既不报错也不出现在 JEI）。
⇒ 教训：**"配方数对得上"不等于"每条配方都能匹配"**，标签文件也要算进资源清单。

---

## 3. 方法签名不再是覆盖 ⇒ 死方法（第二高频 ★★★★★）

### 3.1 症状

功能"写了但没反应"、事件"注册了但不触发"、GUI"能显示但不响应输入"。
**不报错、不崩溃、日志干净** —— 这是最难查的一类。

### 3.2 已知实例（1.20.1 → 1.21.1）

| 方法 | 1.20.1 | 1.21.1 |
|---|---|---|
| `isPathfindable` | 4 参 | 参数不同（原方法变了） |
| `mouseScrolled` | `(double,double,double)` **3 参** | `(double,double,double,double)` **4 参**（多了横向 `scrollX`） |
| `renderBackground` | `(GuiGraphics)` **1 参** | `(GuiGraphics,int,int,float)` **4 参**（1 参已被删除） |
| `defineSynchedData` | `()` + `this.entityData.define(...)` | `(SynchedEntityData.Builder)` + `builder.define(...)` |
| `getEnchantmentLevel` 等 | `EnchantmentHelper.getEnchantments(stack)` 返回 `Map<Enchantment,Integer>` | `Holder<Enchantment>` + `ItemEnchantments` 组件（§4.7） |

### 3.3 ⭐ 最强的检查手段：给所有覆写方法加 `@Override`

javac 对"不是覆盖的方法加 `@Override`"**必定编译报错**。
⇒ **`gradle build` 通过 == 该项目里所有覆写方法都是真覆盖 == 死方法清零。**

这是本项目最有效的一次性排查。建议**每个类都做一遍**（尤其是 GUI/Block/Item/事件监听器）。

### 3.4 ⚠️ 变体：参数个数对了，但含义变了

本项目的真实案例：`mouseScrolled` **本来就已经是 4 参**（所以是真覆盖 ✅），但把**第 3 参（横向 `scrollX`）当成纵向滚动量**在用：

```java
// 错：纵向滚轮时 scrollX 恒为 0 ⇒ 永远不滚，而且 return true 吃掉事件
public boolean mouseScrolled(double x, double y, double delta, double scrollY) {
    if (delta < 0) … // 永远不成立
}
// 对：第 4 参才是纵向
@Override
public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) { … }
```

⇒ 加 `@Override` 只能验证"签名对"，**验证不了"参数含义对"**。参数含义请**从源码 jar 核对分发顺序**（`MouseHandler.onScroll` 里 `d1 = xoffset, d2 = yoffset`）。

### 3.5 ⚠️ 另一个变体：`renderBackground` 导致界面重影

1.21.1 里：

```java
// AbstractContainerScreen
public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
    this.renderTransparentBackground(g);
    this.renderBg(g, pt, mx, my);        // ← 内部已经画了一次 renderBg
}
// 而 AbstractContainerScreen#render 自己就会调 renderBackground
```

⇒ 如果你的 `render()` 里**又显式调了一次** `renderBackground(...)`，`renderBg` 会**跑两遍** ⇒ 界面出现"**一大一小两份重叠**"。

**修法**：把 `this.renderBackground(g, mx, my, pt)` 换成 `this.renderTransparentBackground(g)`（只画变暗背景），**或者直接删掉**（`AbstractContainerScreen#render` 会调）。

本项目在**两个不同模组**里各踩了一次，症状一模一样。

### 3.6 🆕⭐ `Holder` 身份比较：`@Override` 抓不到、也不报错的"死代码"

1.21 把大量"注册表对象"改成了 `Holder<T>`。**`Holder` 是接口，注册表里返回的是 `Holder.Reference`，
而自己写的 `DeferredHolder` 是另一个对象** —— 于是：

```java
// 永远为 true（两个不同的对象）⇒ 整个 if 块是死代码
if (effectInstance.getEffect() != ModEffects.COTH) return;
// 永远为 false
if (effectInstance.getEffect() == this) { … }
```

本项目在 5 处踩到（COTH 自定义处理、深沉潜行移除、末影侵蚀计时器、效果拦截器），
**全部是静默失效**：效果能挂上，但所有"自定义行为"都不执行。

正确写法（任选，推荐第一种）：

```java
if (!effectInstance.getEffect().is(ModEffects.COTH)) return;          // Holder#is(Holder)
if (effectInstance.getEffect().value() != ModEffects.COTH.get()) …    // 比 value
```

⚠️ **反例**：`MobEffects.INVISIBILITY`、`Enchantments.FIRE_ASPECT` 这些是**原版注册表里的 `Holder.Reference`**，
`effect.getEffect() != MobEffects.INVISIBILITY` **是对的**（同一个实例）。**只有和自己注册的
`DeferredHolder` 比才会错**。

⭐ 一次性排查（grep 全工程）：

```
\.getEffect\(\)\s*[!=]=\s*ModEffects\.      ← 可疑
\.getEffect\(\)\s*==\s*this                ← 可疑
\.getEffect\(\)\s*[!=]=\s*MobEffects\.     ← 通常没事（同一个 Reference）
```

---

## 4. 数据/配方/战利品格式（第三高频）★

### 4.1 `result` 格式

```jsonc
// 1.20.1
"result": { "item": "mod:thing", "count": 2 }
// 1.21.1
"result": { "id": "mod:thing", "count": 2 }
```

⚠️ **`result` 写成裸字符串在 1.21 是非法的**（`ItemStack.CODEC` 必须有 `id` 键）：

```jsonc
"result": "mod:thing"        // ❌ 1.21 里解析失败（原版冶炼类配方以前可以这样写）
```

🆕 本项目真实案例：12 条 `smelting/blasting` 配方是从 1.20.1 的裸字符串搬过来的，
`runData`/`runServer` 启动时报 `Parsing error loading recipe`，但**只报一次**、不崩，
如果你不盯日志就会当成"配方没问题"。

### 4.2 ⚠️ 自定义配方类型：有没有 `item→id` 归一化要逐个看

- 有的自定义 `Recipe` 序列化器内部**做了兼容**（`if (copy.has("item") && !copy.has("id")) copy.add("id", copy.remove("item"))`）⇒ 老 JSON 照样能用。
- 但**不是所有**都做了。特别注意"**反射直接委托原版 codec**"的实现（如 `reusable_tool_shaped` 之类）：它拿到的是原版 `ItemStack.STRICT_CODEC` ⇒ **只认 `id`** ⇒ 一条都加载不了。

### 4.3 ⚠️ 自己写的 1.21 codec 极易"读错键"

本项目的真实 bug：迁移时新写的 CODEC 去读一个**根本不存在的键**（如 `input.get("recipe")`），而 JSON 一直是**平铺**的：

```
Parsing error loading recipe mod:xxx: java.lang.IllegalArgumentException: Not a string: null
```

⚠️ 这个报错**非常误导** —— 看起来像"数据缺字段"，其实是**代码读错键**。
**判断法**：把 1.20.1 的 `fromJson` 翻出来，看它到底读哪几个键，和 1.21 的 codec `keys()` 对账。

### 4.4 验证配方是否真的能加载（必做）

```powershell
# ① 从【构建出的 jar 内】逐个解析 JSON，确认 0 语法错误
# ② 再校验每个产出 id 是否真实存在（多源交叉：代码里的注册 + jar 内 models/item + 原版物品列表）
# ③ 边界：result 缺失 / count ≤ 0 / count > 99 / 空 id / result:{} / 用 #tag 当产出
```

⚠️ **"JSON 语法正确" ≠ "配方能加载"**。本项目有 4 条配方语法完美、但**从来没加载成功过**（就是 4.3 那个读错键）。

### 4.5 🆕⭐ 战利品表：`match_tool` 的附魔判定换了一套结构

1.21 的 `ItemPredicate` 把附魔拆到了 `predicates` 子对象里，**旧写法不报错、被静默忽略**：

```jsonc
// ❌ 1.20.1 写法：1.21 里整条 predicate 变成"空条件" ⇒ 永远匹配
"predicate": {
  "items": ["minecraft:iron_pickaxe"],
  "enchantments": [{ "enchantment": "minecraft:silk_touch" }]
}

// ✅ 1.21.1 写法（注意 predicates → minecraft:enchantments → 内层 enchantments）
"predicate": {
  "items": ["minecraft:iron_pickaxe"],
  "predicates": {
    "minecraft:enchantments": [
      { "enchantments": "minecraft:silk_touch" }
    ]
  }
}
```

⚠️ **症状极具误导性**：某类矿石"被挖掘后掉落的是矿石方块本身，而不是对应的掉落物"。
根因是 `match_tool` 的丝精准分支永远成立 ⇒ 走了"精准采集"分支。

⭐ **验证法**：
1. 逐条和**原版同结构文件**（如 `minecraft:coal_ore`）做**结构对比**（键集合 + 嵌套层级），不信肉眼；
2. 改完在专用服务端里 `/setblock` + `/loot`（或直接挖）验证掉落；
3. ⚠️ 注意 `apply_bonus` 里的时运字段**仍然叫 `"enchantment": "minecraft:fortune"`**（
   那是 `apply_bonus` 自己的 codec，和 `match_tool` 的 predicate 不是一套），**不要一起改**。

### 4.6 🆕 用脚本改 JSON 的两个坑

1. **PowerShell 的 `ConvertFrom-Json` 会把只有 1 个元素的数组"拆包"成对象**（再 `ConvertTo-Json` 就写坏了）。
   ⇒ 批量改 JSON **一律用 Python**（`json.load` / `json.dump(ensure_ascii=False, indent=2)`，
   或对"只改一个字段"用**正则只替换第一次出现**，其余原样保留）。
2. 改完先 `git diff --stat` 看**文件数量**是否符合预期，再逐文件 `git diff` 抽查。

### 4.7 🆕 附魔相关 API 全面数据驱动化

```java
// 1.20.1
Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
EnchantmentHelper.setEnchantments(map, stack);
for (Enchantment e : ForgeRegistries.ENCHANTMENTS) if (e.isCurse()) …

// 1.21.1：Holder + 组件 + 标签
HolderSet.Named<Enchantment> curses =
        registryAccess.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.CURSE);
EnchantmentHelper.updateEnchantments(stack, mutable -> {
    if (mutable.getLevel(curseHolder) <= 0) mutable.set(curseHolder, 1);
});
```

⚠️ `Enchantment` 是**动态注册表**（跟着世界/数据包走），必须走 `registryAccess`，不能用 `BuiltInRegistries`。

---

## 5. 方块实体（BE）的三种死法 ★

### 5.1 继承原版"带 BE 的方块"但没覆写 `newBlockEntity()`

```java
public class MySleepingBag extends BedBlock {   // 没覆写 newBlockEntity
}
```

⇒ 放置时走原版 `BedBlock.newBlockEntity()` ⇒ 造出 `BlockEntityType.BED` 的 BE ⇒ `BlockEntity.validateBlockState` 发现"床的 BE 不允许放在睡袋上" ⇒ **抛异常，客户端崩溃**：

```
IllegalStateException: Invalid block entity minecraft:bed ... got Block{mod:my_sleeping_bag}
```

⚠️ 这不只发生在"放置"：**任何 `setBlock`**（躺下/起床翻转属性、`/setblock`）都会走同一条路 ⇒ 都会崩。

⭐ 修法通常是 `newBlockEntity` **返回 `null`**（如果该方块不需要 BE）或注册**自己的 BE 类型**。
⚠️ 注意 `BedBlockEntity` 的构造器**硬编码了 `BlockEntityType.BED` 并在构造里校验** ⇒ 想继承它是个陷阱，别走。

### 5.2 多实例 BE 类型的 `validBlocks` 名单漏登记

```java
BlockEntityType.Builder.of(MyContainerBlockEntity::new, A, B, C, …)   // 漏了新加的 D
```

⇒ 放置 D 就崩，报错同上（`Invalid block entity mod:my_container ... got Block{mod:d}`）。

⭐ **静态对账法（强烈建议做一次）**：

> 列出"**实际会创建该 BE 的方块**"（按类继承链解析 `newBlockEntity`），
> 与 `BlockEntityType.Builder.of(...)` 的**名单**做**差集**。
> 差集 = "一放就崩"的方块清单。

本项目对账 **27 个 BE 类型 / 384 个方块注册**，正好抓出 1 个漏登记的（`medicinal_crate` 漏在 41/42 之外）。这类 bug **只会在玩家放到那个方块时才暴露**。

### 5.3 BE 类型张冠李戴

子类复用**原版** BE 类型（如自己写熔炉却 `super(BlockEntityType.FURNACE, …)`）⇒ 同样崩。
✅ 正确做法：每个自定义 BE 都传自己的 `DeferredHolder`。

### 5.4 存档兼容

- **放宽** `validBlocks`（只加不减）⇒ **对旧存档零风险**。
- **换 BE 类型** ⇒ 旧存档里的旧 `id` 条目会被丢弃（`BlockEntity.loadStatic` 的 `catch (Throwable)` 会打 ERROR/WARN 然后跳过）⇒ **方块还在，但 BE 里的数据没了**。要么接受，要么写 datafix。

### 5.5 🆕 结构里的方块实体 NBT 也可能"张冠李戴"

`.nbt` 结构模板的 `blocks[].nbt.id` 是**方块实体的注册 id**。如果调色板换了方块、但 `nbt` 没删，
放置时会因为 id 与方块不匹配而**打警告并丢弃 NBT**（不崩，但会刷日志）。详见 §13。

---

## 6. 网络包 / 序列化对称性 ★

### 6.1 ⚠️ `ByteBufCodecs.BYTE_ARRAY` 自带 varint 长度前缀

```java
// 错：手写一次长度 + BYTE_ARRAY 又写一次 ⇒ 读端错位、末尾多出几个字节
ByteBufCodecs.VAR_INT.encode(buf, payload.length);
ByteBufCodecs.BYTE_ARRAY.encode(buf, payload);

// 对：交给 BYTE_ARRAY（它自带长度）
ByteBufCodecs.BYTE_ARRAY.encode(buf, payload);
```

症状（客户端直接断线）：

```
DecoderException: Packet ... was larger than I expected, found 3 bytes extra whilst reading
```

> "3 bytes extra" 里的 **3** 就是那个 varint 长度前缀的长度（32768 的 varint 正好 3 字节）。

### 6.2 ⭐ 通用检查：编码后残留 vs 解码后残留

```java
int written = buf.writerIndex();
codec.encode(buf, value);
buf.readerIndex(0);
codec.decode(buf);
if (buf.readableBytes() != 0) → 这条 codec 写得多、读得少（会错位后续数据）
```

⚠️ **"单条往返测试"测不出字节不对称** —— 必须**检查残留**，或把**多条连续写进同一个 buffer** 再连续解。本项目就是"3686 条单条往返全过、客户端仍然崩"。

### 6.3 ⚠️ `ItemStack.STREAM_CODEC` 编解码不对称

- `encode`：空栈 → 抛 `EncoderException("Empty ItemStack not allowed")`
- `decode`：读到 `count <= 0` → 抛 `DecoderException("Empty ItemStack not allowed")`

⇒ 只要服务端能**写出**一个空栈（例如 `new ItemStack(Items.AIR, n)`，它是**空栈但 count > 0**，`isEmpty()` 为 true 却**能通过所有服务端校验**），客户端解码就必炸：

```
Failed to decode packet 'clientbound/minecraft:update_recipes'
  Caused by: DecoderException: Empty ItemStack not allowed
```

**典型成因**：

```java
Item item = BuiltInRegistries.ITEM.get(itemId);          // 未知 id ⇒ 返回 AIR
stack = new ItemStack(item == null ? Items.AIR : item, count);   // ❌ 静默空栈
// ✅ 应改成：未知 id ⇒ 抛异常；最后再 assert !stack.isEmpty()
```

⚠️ 尤其检查**自定义配方类型的结果序列化**，以及任何用 `OPTIONAL_STREAM_CODEC` 的地方。

### 6.4 ⚠️ 两端 mod 集合不一致 ⇒ 注册表 id 错位 ⇒ 同款断线

`Recipe.STREAM_CODEC` 是按**注册表 id** 派发序列化器的：

```java
ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec)
```

⇒ 客户端和服务端 `recipe_serializer` 的**注册顺序**不同 ⇒ 同一个 id 解析成**不同的序列化器** ⇒ 用错的 codec 解字节 ⇒ 读出垃圾 ⇒ 恰好 `count = 0` ⇒ **`Empty ItemStack not allowed`**。

⚠️ `recipe_serializer` / `recipe_type` 属于 **`BuiltInRegistries`（代码注册表）**，**不在** NeoForge 的网络同步注册表列表里 ⇒ **两端不校验、不修正** ⇒ 只要 mod 集合不同就会静默错位。

⭐ **诊断法（很有效）**：两端各打一份 `registry.entrySet()` 的 `#id → 名字` 清单，diff 一下：

```
[registry-audit] side=CLIENT total=55 registry=recipe_serializer
[registry-audit] side=CLIENT #23 sophisticatedcore:upgrade_next_tier
[registry-audit] side=SERVER #23 tacz:gun_smith_table_crafting     ← 从这里开始全错位
```

⇒ 结论通常是"**客户端多了/少了一个贡献注册表的 mod**"或"**顺序不同**"。

### 6.5 🆕 存档也是序列化：`ItemStack#save` 对空栈抛异常

这条和网络包是**同一个根因的两个面**：

```java
public boolean save(CompoundTag tag) {
    if (this.isEmpty()) throw new IllegalStateException("Cannot encode empty ItemStack");  // 1.21 新增
```

⚠️ 1.20.1 只是写一个空 compound，**不报错** ⇒ 从 1.20.1 搬过来的代码里"空栈"到处都是，
它们全都会在**第一次区块卸载**时炸成：

```
An Entity type entity.mod.xxx has thrown an exception trying to write state. It will not persist.
Caused by: java.lang.IllegalStateException: Cannot encode empty ItemStack
```

⭐ **本项目真实案例**（`AbstractArrow` 子类）：

```java
// ❌ 1.20.1 能跑，1.21 每次存档都抛异常，实体直接丢失
super(ModEntities.EGGS.get(), shooter, level, ItemStack.EMPTY, null);
@Override protected ItemStack getDefaultPickupItem() { return ItemStack.EMPTY; }
```

原因：`AbstractArrow` 有个字段 `private ItemStack pickupItemStack = this.getDefaultPickupItem();`，
`addAdditionalSaveData` 里**无条件** `tag.put("item", pickupItemStack.save(...))`。

✅ 修法（两处都要）：

```java
private static final ItemStack PICKUP_ICON = new ItemStack(Items.EGG);   // 非空占位（不会被玩家拿到）
@Override protected ItemStack getDefaultPickupItem() { return PICKUP_ICON.copy(); }
@Override public void addAdditionalSaveData(CompoundTag tag) {
    if (this.getPickupItemStackOrigin().isEmpty()) this.setPickupItemStack(PICKUP_ICON.copy()); // 兜底
    super.addAdditionalSaveData(tag);
}
```

⭐ **全项目排查法**：grep `\.save\(.*registryAccess\(\)\)`、`ItemStack.CODEC`、`\.save\(new CompoundTag\(\)\)`，
逐个确认"这个栈是否可能为空"。

---

## 7. 客户端 / 服务端状态一致性 ★

### 7.1 ⚠️ `common` 包里的 static 会被两端共用（单人档同一 JVM）

本项目真实 case：配方列表放在 `common` 的 static 里 ⇒ 服务端先 `apply` ⇒ 客户端收到包时发现 **revision 已相同 ⇒ `applySnapshotJson` 直接 `return`（不改状态）** ⇒ 而客户端 UI 的刷新条件是 `if (列表引用变了) refresh()` ⇒ **永远不刷新**。

⇒ 表现："服务端/界面数据是新的，但 JEI/EMI 还是旧的"。

**教训**：

- **不要用"状态变了"做刷新条件** —— 改成**无条件刷新（幂等）**。
- 跨端共享的 mutable static 是**危险的**，至少要区分"谁是权威"。

### 7.2 ⚠️ `ItemStack` 没有覆写 `equals` ⇒ 引用比较

```java
// LivingEntity 完成使用物品时（客户端）
if (!this.useItem.equals(this.getItemInHand(hand))) { this.releaseUsingItem(); }
else { …finishUsingItem… }
```

`ItemStack.equals` **没有覆写** ⇒ 这是**引用比较**。

⇒ 如果**使用过程中**改动了手持栈（扣耐久、换 NBT、`shrink`），服务端每 tick 的 `broadcastChanges()` 会让该槽换成**新对象** ⇒ 客户端比对失败 ⇒ **`finishUsingItem` 不执行** ⇒ 依赖它的逻辑（第三方 mod 的开界面、自定义完成逻辑）**全部不触发**。

⇒ 表现：**"有时候生效、有时候不生效"**（取决于这次使用中有没有改动过手持栈）。

**教训**：**不要在 `useItem` 的动画/使用期间改动手持栈**；把消耗放到 `finishUsingItem` 里做。

---

## 8. 渲染与第三方兼容 ★

### 8.1 ⚠️ Sodium 绕过 `BlockRenderDispatcher.renderBatched`

Sodium 用自己的区块网格器（`ChunkBuilderMeshingTask` → `BlockRenderer.renderModel`），**完全不经过** `BlockRenderDispatcher.renderBatched`。

⇒ 任何"靠 hook `renderBatched` 来改方块模型渲染"的代码，在 Sodium 下**是死代码**。
⇒ 表现："选择框转了，但方块模型没转"（因为选框走 `LevelRendererOutlineMixin`，与区块渲染器无关）。

**解决思路**（两条都要）：

1. **把该方块从网格里剔除** ⇒ hook Sodium 的 `LevelSlice.getBlockState` 返回空气 + 自己画；
2. **自己画**（自定义渲染阶段）⇒ 覆盖层。

⚠️ **modid 判定里必须包含 `sodium`**：NeoForge 1.21 的官方 Sodium 移植 modid 就是 `sodium`，它**不声明自己是 `embeddium`**（`embeddium` 只作为 incompatible 依赖列出）。

### 8.2 ⚠️ 第三方 mod 的 jar：不要改，用 mixin

- 只写**我们自己的**代码，用字符串 `targets` + `@Pseudo` + `require = 0`，**不复制对方代码、不改对方 jar、不加编译依赖**。
- ⚠️ 目标类若在**可选 mod** 里 ⇒ 必须放到**独立的 `mixins.json`** 且 `required: false`，**不要**放进主 mixin 配置（主配置若 `defaultRequire: 1`，缺席时会报错）。
- ⚠️ 可选依赖的判定用 `ModList.get().isLoaded("xxx")`，调用对方 API 优先用**反射 + try/catch**，失败就**静默回落**（不要把游戏带崩）。
- ⚠️ **"失败要安全"**：目标 mod 改名/换方法 ⇒ mixin 静默跳过 ⇒ 退回原行为，**不崩**。

### 8.3 ⚠️ 光影（Iris/OptiFine）与"自定义后处理"天生冲突

- 光影会**接管整个渲染管线** ⇒ 你自己画的屏幕空间后处理要么被覆盖、要么位置不对。
- **最稳的方案**：检测"光影是否启用"（Iris 的 `IrisApi.getInstance().isShaderPackInUse()`，用反射），**启用时自动关闭**你的后处理。
- 想真正做到"光影下也有自定义效果" ⇒ 需要：
  - **渲染库**（如 Veil 的 `PostPipeline`）+ **桥接 mod**（如 `iris-veil-compat`，会把你的 shader 源码合并进光影包的 gbuffer 程序）；
  - 或**光影包自己实现**（mod 单方面做不到）。

### 8.4 深度纹理能不能用，必须先验证

后处理这类效果（"不穿墙"/体积光/SSAO）**全部依赖场景深度**。但在光影下，深度纹理**可能被换掉/清空/不是场景深度**。

⭐ **最省事的验证法**：加一个**临时"深度可视化"开关**，把深度画成 `近绿 → 远红`（`r+g=1`、`b` 固定）：

- 能看到**清晰的几何轮廓 + 正确的远近顺序 + 天空纯红 + 有视差** ⇒ **深度可用** ✅
- **整屏单色 / 噪点 / 无视差** ⇒ 深度不可用 ❌
- 屏幕上出现**纯黑/纯白/纯蓝** ⇒ 说明**诊断 shader 根本没跑**（配置没生效 / 回退了别的后端），**不是深度结论** —— 这是那个配色特意设计的排除法。

⚠️ 一定要**开/关光影各测一次**：这样才能区分"是我们的公式错"还是"Iris 下深度不可用"。

### 8.5 🆕⭐ 1.21 的 `Screen#renderBackground` 变成了"高斯模糊 + 菜单底图"

```java
// Screen（1.21.1）
public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(g, mouseX, mouseY, partialTick);   // ← 先画背景
    for (Renderable r : this.renderables) r.render(g, mouseX, mouseY, partialTick);
}
public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
    if (this.minecraft.level == null) this.renderPanorama(g, pt);
    this.renderBlurredBackground(pt);        // ← 高斯模糊（会糊掉整个帧缓冲）
    this.renderMenuBackground(g);            // ← 那张棕色"菜单底图"
    NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));
}
```

⚠️ **症状**：自定义书本/笔记/面板类 GUI 的**内容整体发虚、并且蒙上一层棕色底纹**。

**根因**：这类 GUI 的 `render()` 是"**先画内容、最后调 `super.render`**"的老写法（1.20.1 时代背景只是一层
半透明渐变，盖上去不明显），到 1.21 就变成"**模糊 + 底图糊在内容上面**"。

✅ 修法（三件事一起做）：

```java
@Override
public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
    if (this.minecraft != null && this.minecraft.level != null) {
        g.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010); // 1.20.1 那层渐变
    } else {
        this.renderMenuBackground(g);
    }
    NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));      // 别丢 NeoForge 钩子
}

@Override
public void render(GuiGraphics g, int mx, int my, float pt) {
    this.renderBackground(g, mx, my, pt);        // ① 背景先画
    … 自己的内容（书页/面板）…
    for (Renderable r : this.renderables) {      // ② 不再调 super.render（它会再画一遍背景）
        r.render(g, mx, my, pt);
    }
}
```

⚠️ 反过来也要查：**`AbstractContainerScreen` 子类里手动调 `renderBackground` + `super.render`** 也会画两遍（§3.5）。

---

## 9. 语言 / 资源 / 硬编码 ★

### 9.1 语言键要写**四个**地方

1. `assets/<ns>/lang/en_us.json`
2. `assets/<ns>/lang/<其它语言>.json`
3. datagen 的 **English** lang provider（`this.add("key", "value")`）
4. datagen 的 **另一语言** provider

⚠️ **只改 JSON 不改 datagen provider ⇒ 下次跑 `runData` 会把这些键删掉**（provider 生成的文件会覆盖手写文件）。

### 9.2 硬编码颜色/文本

- `ChatFormatting.RED` / `TextColor.fromRgb(...)` / `§x` 这类写死的颜色，尽量改走**语言或配置**。
- ⚠️ 但**别一刀切**：状态效果、稀有度、类别标识这些颜色是**有语义的**，一刀砍掉会让信息丢失（本项目就回退过一批）。
- ⚠️ **由 id 拼出来的显示名永远不可翻译**（如 `rarity.name().toLowerCase()`）⇒ 必须从语言键取。

### 9.3 🆕⭐ datagen 产物必须**一起搬**（`src/generated/resources`）

上游把 datagen 输出提交在 `src/generated/resources/`。**只搬 `src/main` 会静默丢一大半资源**：

| 丢的东西 | 症状 |
|---|---|
| `lang/*.json` | 创造模式标签页显示 `itemGroup.epca.main_tab` 原文；所有新增物品/方块/字幕都没翻译 |
| `models/item/*.json` | 物品显示**紫黑格子**（missing model） |
| `models/block/*.json` + `blockstates/*.json` | 方块**紫黑格子 / 放下去不渲染** |
| `particles/*.json` | 粒子 `NullPointerException: this.sprites is null`（客户端一放就崩） |
| `recipe/*.json`、`advancement/*.json`、`tags/` | 配方/进度/标签缺失 |

✅ 做法：
1. 把上游 `src/generated/resources` **原样**复制进工程，并在 `build.gradle` 里把它加进 source set
   （或像本项目一样让 `processResources` 一起打包）；
2. 两个 source set 有同路径文件时，`processResources { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }`；
3. **跑一次 `runServer` 看有没有 `Missing model` / `Unable to load` 警告**，比一个个翻文件快。

⚠️ 本项目还踩了一个衍生问题：4 个物品上游**根本没有贴图**（只有注册代码），
所以即使资源搬全了它们还是紫黑格子 ⇒ 只能生成占位图（并在文档/NOTICE 里注明"占位美术"）。

---

## 10. 工程流程 / 工具经验（能省大量时间）★

### 10.1 构建

```powershell
$env:GRADLE_USER_HOME='D:\gradle-home'; $env:JAVA_HOME='D:\zulu21'
$env:GRADLE_OPTS='-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US'
& '<gradle>\bin\gradle.bat' build --console=plain -p '<工程目录>'
```

- ⚠️ **不要加 `--offline`**（依赖可能没缓存）。
- ⚠️ **UP-TO-DATE 但 jar 时间戳是旧的** ⇒ 加 `clean build` 强制重建（本项目踩过：改完 sources 但产物没更新）。
- ⚠️ **复制 jar 前必须检查 `$LASTEXITCODE`**（本项目踩过：构建失败但 jar 是上一次的，被当成新产物交付）。
- ⚠️ **不要和另一个人/agent 同时构建同一工程**（会抢锁或拿到半成品）。
- 🆕 编译错误太多被截断 ⇒ `compileJava.options.compilerArgs << '-Xmaxerrs' << '3000'`，
  或先只修"第一批"错误再看下一批。
- 🆕 镜像/网络：`maven.neoforged.net` 在国内经常不可达。可以写 `~/.gradle/init.gradle`
  把 `maven.neoforged.net` 重写到 `neoforged.forgecdn.net`：

```groovy
// init.gradle
allprojects {
    repositories {
        maven { url 'https://neoforged.forgecdn.net/releases' }
        maven { url 'https://neoforged.forgecdn.net/maven' }
    }
}
```

### 10.2 查真实签名（**最高频、最有用**）

MC/NeoForge 的源码就在工程里：

```
<工程>/build/moddev/artifacts/neoforge-21.1.220-sources.jar        ← 源码，直接看
<工程>/build/moddev/artifacts/neoforge-21.1.220-merged.jar         ← 字节码，javap 用
<工程>/build/moddev/artifacts/*client-extra*aka-minecraft-resources.jar   ← 原版资源（验证数据包目录名）
```

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead('<...>/neoforge-21.1.220-sources.jar')
$e=$z.Entries | Where-Object { $_.FullName -eq 'net/minecraft/.../Xxx.java' }
[System.IO.Compression.ZipFileExtensions]::ExtractToFile($e,"$env:TEMP\Xxx.java",$true)
$z.Dispose()
```

```powershell
javap -p -c -classpath '<...>/neoforge-21.1.220-merged.jar' net.minecraft.world.item.ItemStack
```

### 10.3 备份与对账（必做）

- 改前 **`src` 全量备份** 到 `.tmp/<name>-backup-<时间戳>/`。
- 改完用 **逐文件 hash 对比**备份，确认"**只改了预期的那几个文件**"（能立刻发现误伤）。

### 10.4 ⚠️ PowerShell 的 `Copy-Item -Recurse` 会嵌套目录

```powershell
# 目标目录已存在时会变成 <目标>/<源目录名>/…   ⇒ data/forge/forge/…
Copy-Item $from -Destination $to -Recurse -Force
```

⇒ **先删目标**再复制，或用 `robocopy`，或**逐文件移动**。本项目因此把 18 个标签文件搬进了一个不存在的路径，**看起来"文件在"、实际全失效**。

### 10.5 拿 1.20.1 参考树 diff

把两个工程的同名文件直接对比（`git diff --no-index` 或人工看），**唯一实质差异往往就是根因**。
本项目的经验：客户端渲染/GUI 类文件 90% 逐行一致，**差异就在那一两行**。

🆕 补充：**"逐行一致"也可能是 bug** —— 如果那一行在 1.21 的语义变了（如 §3.6 的 `Holder` 比较、
§8.5 的 `render` 顺序、§12 的 `hurt` 早退），照抄反而会出问题。**diff 只能证明"没改"，不能证明"对"**。

### 10.6 🆕 访问转换器（AT）必须用**官方（Mojang）名**

1.21 NeoForge 的 AT 走 official mappings，1.20.1 的 SRG 名字**一个都不认**。本项目实际用到：

```
public net.minecraft.world.item.CreativeModeTab displayItems
public net.minecraft.world.item.CreativeModeTab displayItemsSearchTab
public net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu getRowIndexForScroll(F)I
public net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen selectedTab
public net.minecraft.world.entity.projectile.AbstractArrow setPierceLevel(B)V
```

⚠️ AT 写错会**在构建期直接报错**（"cannot find field/method"），这是少数"错了会响"的地方，
所以改完 AT 一定要 `clean build` 一次。

---

## 11. 🆕 无窗口验证方法论（本 v2 最重要的一节）

> 需求场景：用户明确要求"不要在我电脑上弹 MC 窗口"。于是**所有**验证必须在专用服务端完成。
> 这一节就是"没有客户端怎么把 bug 复现/验证清楚"的完整套路。

### 11.1 基础设施：RCON

`run/server.properties` 里加：

```properties
enable-rcon=true
rcon.password=test123
rcon.port=25575
online-mode=false
max-tick-time=-1
```

启动：`gradlew runServer`（后台 job），等日志出现 `Done (x.xxxs)!` 和 `RCON running on ...`。

PowerShell 最小 RCON 客户端（协议：`[i32 长度][i32 请求id][i32 类型][payload][\0\0]`，类型 3=登录、2=命令）：

```powershell
function Rcon-Cmd([string]$cmd, [int]$id) {
  $c = New-Object System.Net.Sockets.TcpClient; $c.Connect('127.0.0.1', 25575)
  $s = $c.GetStream()
  # 登录
  $pl = [Text.Encoding]::UTF8.GetBytes('test123')
  $ms = New-Object IO.MemoryStream; $bw = New-Object IO.BinaryWriter($ms)
  $bw.Write([int]($pl.Length + 10)); $bw.Write([int]0); $bw.Write([int]3); $bw.Write($pl); $bw.Write([byte]0); $bw.Write([byte]0)
  $b = $ms.ToArray(); $s.Write($b, 0, $b.Length); $s.Flush(); Start-Sleep -Milliseconds 250
  [void]$s.Read((New-Object byte[] 4096), 0, 4096)
  # 命令
  $pl2 = [Text.Encoding]::UTF8.GetBytes($cmd)
  $ms2 = New-Object IO.MemoryStream; $bw2 = New-Object IO.BinaryWriter($ms2)
  $bw2.Write([int]($pl2.Length + 10)); $bw2.Write([int]$id); $bw2.Write([int]2); $bw2.Write($pl2); $bw2.Write([byte]0); $bw2.Write([byte]0)
  $b2 = $ms2.ToArray(); $s.Write($b2, 0, $b2.Length); $s.Flush(); Start-Sleep -Milliseconds 700
  $buf = New-Object byte[] 65536; $r = $s.Read($buf, 0, $buf.Length)
  $text = ''
  if ($r -gt 12) { $len = [BitConverter]::ToInt32($buf, 0); $text = [Text.Encoding]::UTF8.GetString($buf, 12, [Math]::Min($r - 14, $len - 10)) }
  $c.Close(); $text
}
```

### 11.2 ⭐ 核心手法：临时调试命令 + `FakePlayer`

在模组里加一个**临时**命令类（发布前删掉），把"需要真玩家才能触发"的逻辑变成一行命令：

```java
public static LiteralArgumentBuilder<CommandSourceStack> register() {
    return Commands.literal("epca_debug_attack").executes(ctx -> {
        var src = ctx.getSource();
        var level = src.getLevel();
        FakePlayer fake = FakePlayerFactory.getMinecraft(level);   // 假玩家
        // … 构造实体、调用 hurt/playerTouch/quickMoveStack …
        return 1;
    });
}
```

配合 `src/…/epca.java` 里的 `onRegisterCommands`：
`event.getDispatcher().register(DebugXxxCommand.register());`

**这招能验证的东西**（本项目全部实测过）：
实体伤害链路、`/kill` 是否生效、假死状态机、`draw`/`pickup` 逻辑、
`EnchantmentMenu` 的槽位是否接受某物品、`quickMoveStack` 的归位、
实体 NBT 保存/读取、弹射物的 `AbstractArrow` 序列化。

### 11.3 ⚠️ 四个必踩的坑

1. **`FakePlayer` 默认无敌** —— `isInvulnerableTo()` 直接 `return true`。
   测伤害要写一个子类覆盖它（或 `setInvulnerable(false)` 没用，因为被 override 掉了）：
   ```java
   static final class TestPlayer extends FakePlayer {
       TestPlayer(ServerLevel level) { super(level, new GameProfile(UUID.randomUUID(), "TESTER")); }
       @Override public boolean isInvulnerableTo(DamageSource src) { return false; }
   }
   ```
2. **`ServerPlayer.spawnInvulnerableTime` 是 private** —— 新建的假玩家带着出生保护，
   `hurt()` 会直接返回 false（而且**不报错**）。必须用反射清零：
   ```java
   var f = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
   f.setAccessible(true); f.setInt(player, 0);
   ```
3. **没有玩家时区块可能不 tick** —— 实体不会动、`deathTime` 不涨、AI 不跑。
   调试命令里手动强制加载：
   ```java
   level.setChunkForced(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z), true);
   ```
4. **`/place structure` ≠ `/place template`** —— 前者放的是 `worldgen/structure`（拼图结构，
   会自己找位置/可能放偏），后者才是 `data/<ns>/structure/*.nbt`。**验证结构模板必须用 `place template`**。

### 11.4 命令层断言技巧

| 目的 | 命令 | 判读 |
|---|---|---|
| 某坐标是不是某方块 | `execute if block x y z minecraft:foo` | RCON 返回 `Test passed` / `Test failed` |
| 统计一片区域里的方块数 | `fill x1 y1 z1 x2 y2 z2 minecraft:air replace minecraft:foo` | 返回 `Successfully filled N block(s)` |
| 读实体 NBT | `data get entity @e[type=…,limit=1]` | — |
| ❌ 别用 | `execute if block … run say …` | `say` 走聊天广播，**RCON 拿不到回显** |

### 11.5 mixin 到底有没有生效：`-Dmixin.debug.export=true`

`build.gradle` 的 `runs { configureEach { systemProperty 'mixin.debug.export', 'true' } }`，
跑一次后会在 `run/.mixin.out/class/` 下导出**转换后**的类，然后：

```powershell
javap -p -c run\.mixin.out\class\net\minecraft\world\inventory\EnchantmentMenu.class |
  Select-String 'handler\$|redirect\$|invokespecial'
```

⭐ 本项目靠它抓到一个**极隐蔽**的问题：`@Inject(method = "<init>", …)` **只匹配第一个构造器**。
`EnchantmentMenu` 有两个构造器，我们注入了两参那个（委托构造器），而服务端实际用的是三参那个 ⇒
**handler 合并进去了、但永远不执行**。必须写完整描述符：

```java
@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
        at = @At("TAIL"))
```

### 11.6 判定"事件有没有被取消"

在调试命令里注册一个 **LOWEST 优先级**监听，把状态打进一个 static 字段：

```java
NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LivingIncomingDamageEvent e) -> {
    if (e.getEntity() instanceof TestPlayer) LAST_STATE = "cancelled=" + e.isCanceled();
});
```

如果这个监听**根本没被调用**，说明伤害在**到达事件之前**就被拦了
（`isInvulnerableTo` / `isDeadOrDying` / `abilities.invulnerable`）—— 这会立刻把排查方向掰正。

### 11.7 收尾纪律

- 调试命令、`mixin.debug.export`、`enable-rcon` 都是**临时**的，发布前全删/全恢复；
- 每次跑完 `runServer`，把残留的 `java.exe`（命令行含工程名）结束掉，并删 `run/world/session.lock`，
  否则下一次启动会报"世界正在使用中"；
- 最终产物必须再做一次 `clean build` + `runServer` 冒烟（`Done (x.xxx s)`、0 条 `ERROR`）。

---

## 12. 🆕 实体 / 伤害 / 死亡语义（1.21 与"想当然"的差异）

### 12.1 `LivingEntity#hurt` 的早退顺序（决定了"打不动"的所有可能）

```java
public boolean hurt(DamageSource source, float amount) {
    if (this.isInvulnerableTo(source)) return false;              // ① 无敌
    if (this.level().isClientSide) return false;
    if (this.isDeadOrDying()) return false;                       // ② 血已 0（关键！）
    if (source.is(IS_FIRE) && this.hasEffect(FIRE_RESISTANCE)) return false;
    // … 推 DamageContainer，触发 LivingIncomingDamageEvent（③ 事件可取消）
```

⚠️ **② 是最坑的一条**：`isDeadOrDying()` 只看 `getHealth() <= 0`。
一个"血 0 但没被移除"的实体会**永久免疫一切伤害，`/kill` 也一样**（因为 `/kill` 也是走 `hurt`）。
而且它**不是"死"**：`isAlive()` 是 `!isRemoved() && health > 0` ⇒ `false`，但实体还在世界里、还能被看到。

⭐ **三种制造这种"僵尸状态"的路径**（本项目全踩过）：
1. `die()` 里 `event.setCanceled(true)`（`LivingDeathEvent`）后没有正确恢复；
2. 假死/死亡动画的计时器**没有 tick**（区块不 tick、`NoAI` 里漏了、`shouldTickDeath` 为 false）；
3. `setHealth(0)` 被直接调用（绕过 `actuallyHurt` 的 `die`）。

✅ 兜底三件套（建议所有自定义生物都加）：

```java
// ① 看门狗：0 血且非"演出中"超过 N tick ⇒ 直接移除
if (!level().isClientSide && isDeadOrDying() && !isFakingDeath() && !isRemoved()) {
    if (++stalledTicks > 60) this.remove(Entity.RemovalReason.KILLED);
} else stalledTicks = 0;

// ② 让 /kill（BYPASSES_INVULNERABILITY）永远有效，包括"演出期间"
boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
if (isFakingDeath() && !bypass) return false;
if (bypass && isDeadOrDying()) { this.remove(Entity.RemovalReason.KILLED); return true; }

// ③ die() 里遇到 bypass 时跳过一切"复活/演出"逻辑
if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) { super.die(source); return; }
```

### 12.2 `isInvulnerable()` 覆盖 = 连 `/kill` 都挡

很多模组喜欢写：

```java
@Override public boolean isInvulnerable() { return this.entityData.get(DATA_INVULNERABLE); }
@Override public void setInvulnerable(boolean v) { this.entityData.set(DATA_INVULNERABLE, v); }
```

⚠️ 这两个覆盖会**顶掉** `Entity` 原生的 `invulnerable` 字段（以及它的 NBT 存档），
而 `hurt()` 里那种 `if (isInvulnerable()) return false;` **不检查 `DamageTypeTags.BYPASSES_INVULNERABILITY`** ⇒
`/kill`、虚空伤害全部失效。**永远要加 bypass 判断**：

```java
if (isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
```

### 12.3 "假死/死亡演出"必须可恢复

本项目上游有一个 40% 概率的"假死"演出（`setInvulnerable(true)` + `NoAI` + 血量压到 0.02 + `Pose.DYING`，
30 tick 后爆开并 `discard()`）。设计是好的，但**边界没兜**：

| 漏掉的地方 | 后果 |
|---|---|
| 演出中 `hurt()` 无条件 `return false` | `/kill` 无效（原版规范被吃掉） |
| 演出结束只 `discard()`，**不复位** `invulnerable/NoAI` | 只要 `discard()` 没执行到，就留下"无敌 + NoAI + 0.02 血"的残骸 |
| 状态只放 `SynchedEntityData`，**不写 NBT** | 演出途中存档/重进 ⇒ 状态丢失，留下 0.02 血、站着不动的怪 |
| 没有"0 血未移除"的兜底 | 见 §12.1 |

✅ 正确做法：
- 演出状态**写进 `addAdditionalSaveData`**（`EpcaFakeDeath` + 剩余 tick），
  `readAdditionalSaveData` 里**继续跑完**而不是当没发生；
- 读档时顺便修复旧存档里的残骸（`NoAI && health <= 0.02` ⇒ 解除 `NoAI`、补到 1 血）；
- 演出**结束**时复位 `invulnerable/NoAI`（即使马上要 `discard`，多一层保险）。

⚠️ 注意：`SynchedEntityData` 里的 flag **不会进存档**（只同步给客户端），
而 `Entity.addAdditionalSaveData` 存的是**字段** `this.invulnerable` —— 所以"用 entityData 覆盖
`setInvulnerable`"的写法，其无敌状态**读档后就消失了**。这类"字段 vs entityData"的分裂要特别小心。

### 12.4 实体属性注册（`EntityAttributeCreationEvent`）

漏注册一个实体类型的属性 ⇒ `MAX_HEALTH` 默认 0 ⇒ 生物**一生成就 0 血/打一下就没/或者干脆不刷**。
`getAttributeValue(ATTACK_DAMAGE)` 为 0 时表现为"有仇恨、会挥手，但不掉血"。

✅ 排查：`EpcaEntityManager.createAttributes(event)` 里逐类型 `event.put(type, supplier)`，
再和"所有 `DeferredHolder<EntityType>` 注册"做**差集**。

### 12.5 弹射物的"拾取"语义（`AbstractArrow`）

| 值 | 谁能捡 | 捡到给物品吗 |
|---|---|---|
| `Pickup.DISALLOWED` | 没人 | — |
| `Pickup.CREATIVE_ONLY` | 只有创造玩家 | **不给**（碰一下就消失） |
| `Pickup.ALLOWED` | 所有人 | 给 `getPickupItem()` |

⚠️ 常见错误：自定义 `playerTouch` 里只处理 `ALLOWED`，把 `CREATIVE_ONLY` 忘了 ⇒
**创造模式投出去的弹射物永远躺在地上**（本项目 8 个长矛类全部如此，上游同样）。

✅ 修法：

```java
if (this.wasCreativeThrower || this.pickup == Pickup.CREATIVE_ONLY) {
    if (player.getAbilities().instabuild) { playSound(...); this.discard(); }  // 只让创造玩家清掉
    return;
}
if (this.pickup == Pickup.ALLOWED) { …正常入手… }
```

### 12.6 食物/饱和度的语义变了

1.20.1 的 `FoodProperties` 存的是**饱和度修饰符**，1.21 存的是**算好的饱和度**（`nutrition * modifier * 2`）。
从 1.20.1 搬来的"生物质 = 饥饿值 + 饱和度"这类公式必须**先换算回修饰符**，否则数值翻倍。

---

## 13. 🆕 结构模板（`.nbt`）的三类坑

### 13.1 调色板里引用了"不存在的 mod 的方块"

```jsonc
// 结构调色板（解压后的 NBT 文本化示意）
{ "Name": "eej:packed_mud_altar_stone" }   // ← eej 没有 1.21.1 版本
```

⇒ 生成的建筑**那部分是空洞**，游戏**不报错**（结构照样"生成成功"）。

✅ 处理：把外来方块换成**同风格的原版方块**，并**顺便删掉它对应的方块实体 NBT**。

本项目映射（保持"本来就在同一套结构里出现过"的原则，避免风格断裂）：

```
eej:packed_mud_altar_stone → minecraft:packed_mud
eej:packed_mud_pedestal    → minecraft:mud_bricks   （并删除 nbt: {id, inventory, filterData}）
```

### 13.2 不能用文本替换改 NBT

NBT 的字符串是 `[u16 长度][UTF-8 字节]`，**改长度就会破坏整个流**。
必须用真正的解析器（自己写 100 行也能搞定，或 `python -m pip install nbtlib`）：

```python
# 最小读写（gzip + 全 tag 类型）——本项目实际用的就是这套
def load(path):  …  # gzip.decompress → 递归读 tag
def save(path, name, root): …  # 递归写 tag → gzip.compress(..., mtime=0)
```

⚠️ 写完必须**自检**：`size` 没变、`blocks` 数量没变、`state` 索引 < 调色板长度、
命名空间只剩 `minecraft`/`<自己>`。

### 13.3 放置验证要用 `place template`

```mcfunction
forceload add 0 0
place template epca:abandoned_cake_workshop 4 200 4
execute if block 4 200 4 minecraft:grass_block        # 结构里 (0,0,0) 的方块
fill 4 200 4 26 211 25 minecraft:air replace minecraft:mud_bricks   # 统计数量
```

本项目验证结果：`mud_bricks 175 块 / packed_mud 39 块`，与 NBT 解析统计**逐一对上**，
服务端日志 0 条 `ERROR`。

---

## 14. 🆕 可选依赖隔离（`NoClassDefFoundError` 的正确姿势）

**症状**：服务端/客户端启动时 `NoClassDefFoundError: dev/kosmx/playerAnim/api/…`，但"我只是加了个可选联动"。

**根因**：只要**类加载**到任何引用可选 mod 的类，就会去解析它的类型 —— 哪怕那行代码永远不执行。
`@EventBusSubscriber` 标注的类会在构造期被**主动加载**，最容易踩。

✅ 三步隔离：

1. **把对可选 mod 的所有引用关进一个类**（`PlayerAnimCompat`），其它地方**只碰这个类**；
2. 加一个**不加载任何对方类型**的探针：

```java
public final class PlayerAnimSupport {
    private static final boolean AVAILABLE = probe();
    private static boolean probe() {
        try { Class.forName("dev.kosmx.playerAnim.api.layered.IAnimation", false,
                PlayerAnimSupport.class.getClassLoader()); return true; }
        catch (Throwable t) { return false; }
    }
    public static boolean available() { return AVAILABLE; }
}
```

3. **删掉那些 `@EventBusSubscriber` 标注的可选联动类**（改成在主类里 `if (PlayerAnimSupport.available()) NeoForge.EVENT_BUS.register(…)`），
   并把 `neoforge.mods.toml` 里的依赖标成 `optional = true`。

⚠️ 别在 `static final` 字段的初始化里直接引用对方类型（类初始化会连带解析），
要么延迟到方法内，要么整段放进 `if (available())` 分支的**独立方法**里。

---

## 15. 🆕 mixin 实战：从"能用"到"不会把别人的游戏搞崩"

### 15.1 `@Redirect` 撞车 ⇒ 优先 `@WrapOperation`（MixinExtras）

`@Redirect` 是**独占**的：装了两个都 redirect 同一处的 mod，后加载的那次会**直接崩**：

```
@Redirect conflict. Skipping … from mod A with priority 1000, already redirected by mod B
```

✅ 改用 MixinExtras 的 `@WrapOperation`（**可叠加**、可 `require = 0` 静默降级）：

```java
@WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(...)"),
        require = 0)
private static ResourceLocation epca$tint(LivingEntityRenderer<?, ?> self, LivingEntity e,
        ResourceLocation original, Operation<ResourceLocation> op) { … }
```

### 15.2 客户端 mixin 单独一个配置，并且 `defaultRequire: 0`

渲染类 mixin 最容易和 Sodium/Iris/加速渲染 mod 撞车。做法：

- `epca.mixins.json`（common）：`"required": true`、`"injectors": { "defaultRequire": 1 }`；
- `epca.client.mixins.json`（client）：**`"required": false` + `"defaultRequire": 0`**，
  `neoforge.mods.toml` 里两个 `[[mixins]]` 都声明。

这样客户端渲染 mixin 失败时**只是不生效**，不会把游戏带崩。

### 15.3 注入点要"容错"

- 能给 `require = 0` 就给（尤其是 `@WrapOperation` / `@Redirect` / 目标方法可能被别的 mod 改）；
- **不要 `@Overwrite`**（本项目里唯一的 `@Overwrite` 是 `PointedDripstoneBlock`，属于"不改就没法玩"的情况）；
- 注入"构造器"时**写完整描述符**（§11.5）；
- `@Shadow` 字段/方法要与目标**同签名**，否则 `@Shadow` 校验失败会崩（这个在 dev 里会立刻报）。

### 15.4 诊断工具

- `-Dmixin.debug.export=true` + `javap`（§11.5）；
- `-Dmixin.debug.verbose=true` 可以看到每个注入点的匹配位置；
- 崩在启动阶段时，`run/logs/latest.log` 里搜 `Mixin apply failed` / `was not found` / `Injection failure`。

---

## 16. 🆕 原版硬编码与"槽位/规则"改动

### 16.1 附魔台的青金石槽是**写死**的

```java
// EnchantmentMenu（1.21.1）：匿名 Slot 里
public boolean mayPlace(ItemStack stack) {
    return stack.is(Items.LAPIS_LAZULI);   // NeoForge 源码里还留着 TODO: 以后改成 tag
}
// quickMoveStack 里还有同一个判断
} else if (itemstack1.is(Items.LAPIS_LAZULI)) { … }
```

⇒ **任何"青金石类"自定义物品都放不进去**（不是标签能解决的）。

✅ 修法（不改匿名类）：**把 1 号槽换掉**。

```java
@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
        at = @At("TAIL"))
private void epca$acceptModdedFuel(CallbackInfo ci) {
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
    Slot old = menu.slots.get(1);
    menu.slots.set(1, new EnchantingFuelSlot(old.container, 1, old.x, old.y));
}
```

`EnchantingFuelSlot extends Slot`：`mayPlace` 接受原版青金石 + `c:enchanting_fuels` 标签 + 自己的物品；
`getNoItemIcon()` 返回 `Pair.of(InventoryMenu.BLOCK_ATLAS, ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli"))`。

⚠️ 还要处理 `quickMoveStack` 里的同一个判断（Shift+左键归位）：

```java
@Redirect(method = "quickMoveStack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"),
        require = 0)
private boolean epca$countFuelAsLapis(ItemStack stack, Item item) {
    return stack.is(item) || EnchantingFuelSlot.isFuel(stack);
}
```

⭐ 好消息：菜单其余逻辑（费用计算、`clickMenuButton` 的消耗、`getGoldCount()`）**只清点数量**，
不校验物品 id —— 所以"能放进去"就等于"能当青金石烧"，不需要再 hook。

### 16.2 其它"你以为能改、其实写死"的地方（先 grep 再动手）

- 铁砧/砂轮的输入校验；`GrindstoneMenu.mayPlace`；
- `BrewingStandMenu` 的燃料/药水位；
- `BeaconMenu` 的付款槽（本项目就是靠 mixin 改的）；
- `ItemCombinerMenu` 一类"只接受某 tag/某物品"的判定。

**通用做法**：`grep "mayPlace" <原版源码>`，把判定为 `is(Items.X)`（**物品身份**，不是 tag）的地方列出来，
这些就是"自定义同类物品永远进不去"的位置。

---

## 17. 🆕 发布与 CI（把"能跑"变成"能交付"）

### 17.1 交付物

```
dist/<mod>-<version>.jar
dist/SHA256SUMS.txt
```

- 每次构建**先看 `$LASTEXITCODE`**，再把 `build/libs/*.jar` 复制到 `dist/`；
- 生成 `SHA256` 并写进 Release 说明；
- 交付前 `unzip -l`/`javap` 抽查**关键新类**是否真的在 jar 里（§10.1 的"假产物"教训）。

### 17.2 GitHub Actions（NeoForge 模板）

```yaml
name: Build
on: { push: { branches: [main] }, pull_request: , workflow_dispatch: }
permissions: { contents: read }
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 40
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew build --no-daemon --stacktrace
      - uses: actions/upload-artifact@v4
        with: { name: mod-jar, path: build/libs/*.jar, if-no-files-found: error }
```

⚠️ **两个必做**：
1. `git update-index --chmod=+x gradlew`（Linux runner 上 `./gradlew` 必须可执行，
   在 Windows 上提交默认是 644，CI 会 `Permission denied`）；
2. `.gitattributes` 里 `*.jar binary`、`*.nbt binary`（§1.5）。

再加一个**零依赖**的"数据体检" job，能挡住 90% 的数据格式回归：

```yaml
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: |
          python3 - <<'PY'
          import json, pathlib, sys
          bad = []
          for p in sorted(pathlib.Path('src').rglob('*.json')):
              try: json.loads(p.read_text(encoding='utf-8'))
              except Exception as e: bad.append(f'{p}: {e}')
          print(f'checked {len(list(pathlib.Path("src").rglob("*.json")))} files')
          if bad: print('\n'.join(bad[:50])); sys.exit(1)
          PY
```

### 17.3 推送与发布的两个坑

1. **推 `workflow` 文件需要额外 scope** —— 只有 `repo` 的 classic PAT 会报：
   ```
   refusing to allow a Personal Access Token to create or update workflow `.github/workflows/build.yml`
   without `workflow` scope
   ```
   ⇒ 生成 PAT 时勾 `repo` + `workflow`（或先用网页把 workflow 文件加上）。
2. **GitHub 的连接在国内会随机重置** —— push 要带重试，并关掉慢速超时：

```powershell
$b64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("user:PAT"))
git -c http.version=HTTP/1.1 -c http.postBuffer=104857600 `
    -c http.lowSpeedLimit=0 -c http.lowSpeedTime=0 `
    -c "http.extraHeader=Authorization: Basic $b64" push https://github.com/u/r.git main:main
```

⚠️ 别把 token 写进 remote URL / `.git/config`；用完提醒用户 Revoke。

---

## 18. 症状 → 原因 速查表（v2：30 条）

| 症状 | 最可能的原因 | 看第几节 |
|---|---|---|
| 配方/战利品/进度/标签**全不生效** | 数据包目录还是**复数** | §1 |
| 某个功能"写了但没反应" | 方法**签名不再是覆盖**（死方法） | §3 |
| 效果挂上了但**自定义行为不执行** | `Holder != DeferredHolder` 恒为真 | §3.6 🆕 |
| GUI **一大一小两份重叠** | `renderBg` 跑了**两次**（重复调 `renderBackground`） | §3.5 |
| 自定义书本/笔记 GUI **整体发虚 + 棕色底纹** | 1.21 的 `renderBackground` 加了模糊+菜单底图，且被画在内容之后 | §8.5 🆕 |
| 滚轮/输入**完全没反应** | `mouseScrolled` 参数个数**或含义**不对 | §3.2 / §3.4 |
| **放置某个方块立刻崩** | BE 的 `newBlockEntity` 没覆写 / `validBlocks` 漏登记 | §5 |
| 结构放置处**出现空洞** | 调色板引用了不存在 mod 的方块 | §13.1 🆕 |
| 结构模板**加载失败/解析异常** | `.nbt` 被 git 的 CRLF 转换改坏 | §1.5 🆕 |
| **进存档断线**（`DecoderException`） | 包序列化错位 / 空 ItemStack / **两端注册表不一致** | §6 |
| `custom_payload ... found N bytes extra` | codec **写得多、读得少** | §6.1 / §6.2 |
| `update_recipes` + `Empty ItemStack not allowed` | 服务端写出空栈 / 或者两端 `recipe_serializer` 顺序不同 | §6.3 / §6.4 |
| 区块卸载时 `Cannot encode empty ItemStack`（实体丢失） | 实体 NBT 里存了空 ItemStack（1.21 会抛异常） | §6.5 🆕 |
| 物品**挖掘后掉落方块本身** | `match_tool` 的附魔 predicate 还是旧结构（静默变空条件） | §4.5 🆕 |
| 某条配方**永远匹配不上** | 材料标签文件缺失/为空（如 `c:xxx` 没搬过来） | §2.3 🆕 |
| 物品**紫黑格子** / 语言显示 key 原文 / 粒子 NPE | datagen 产物（`src/generated/resources`）没搬 | §9.3 🆕 |
| 客户端**显示不刷新** | `common` static 共用 + "状态变了才刷新" | §7.1 |
| 某个操作**有时生效有时不生效** | 客户端预测 + `ItemStack` **引用比较** | §7.2 |
| 某方块在 **Sodium** 下不渲染 / 只转选框 | 依赖 `renderBatched` | §8.1 |
| 装了光影后自定义效果**失效/花屏** | 后处理与光影冲突 | §8.3 |
| 语言键**重跑 datagen 后消失** | 只改了 JSON、没改 provider | §9.1 |
| 生物**打不死、血量 0 但还活着、`/kill` 无效** | 0 血未移除 ⇒ `LivingEntity#hurt` 早退 + `hurt` 覆盖挡了 bypass | §12.1 🆕 |
| 生物**偶尔**打不死（概率性） | 假死/死亡演出卡在没 tick/没写档的状态 | §12.3 🆕 |
| 生物**有仇恨、会挥手但不掉血** | 实体属性没注册（`ATTACK_DAMAGE`/`MAX_HEALTH` 为 0） | §12.4 🆕 |
| 创造模式投出的弹射物**永远躺在地上** | `playerTouch` 只处理了 `Pickup.ALLOWED` | §12.5 🆕 |
| 自定义"青金石/燃料/付款"物品**放不进原版槽位** | 原版 `mayPlace` 里是 `is(Items.X)` 硬编码 | §16.1 🆕 |
| 启动时报 `NoClassDefFoundError: <可选mod>` | 有类在加载期引用了可选依赖类型 | §14 🆕 |
| 装渲染优化 mod 后**启动崩 / `@Redirect` 冲突** | `@Redirect` 独占，必须换 `@WrapOperation` + `defaultRequire: 0` | §15 🆕 |
| 注入"成功"但**永远不执行** | `method = "<init>"` 只匹配第一个构造器 | §11.5 🆕 |
| CI 上 `./gradlew: Permission denied` | `gradlew` 在 git 里没有可执行位 | §17.2 🆕 |
| 配置项改了**没效果** | 需要重启 / 键被覆盖 / 读错了键名 | §4.3 / §10 |

---

## 19. 建议的移植顺序（照这个走能少返工）

1. **先编译跑通**（`gradle build`）—— 把所有编译错误清掉。
2. ⭐ **给所有覆写方法加 `@Override`** —— 一次性清掉所有"死方法"（§3）。
3. ⭐ **数据包目录全量改名**（§1）—— 从原版 jar 取权威目录名，改完从**出包 jar** 里验证无复数残留。
4. **资源完整性**（§9.3）—— `src/generated/resources` 一起搬；跑一次 `runServer` 看 `Missing model`。
5. **标签命名空间对账 + 官方发布包 diff**（§2）—— 代码侧 `TagKey` ↔ 文件是否存在/非空；发布包里有、工程里没有的标签补回来。
6. **配方/战利品格式体检**（§4）—— 从 jar 内解析 + 产出 id 是否存在 + 和原版同结构文件**结构对比**（§4.5）。
7. ⭐ **BE 全量对账**（§5）—— `newBlockEntity` 是否覆写 / `validBlocks` 差集是否为空。
8. **网络包对称性**（§6）—— 编码残留 == 0；空栈/空 ItemStack 不允许上线；实体 NBT 里的空栈也要清（§6.5）。
9. **实体/伤害语义复查**（§12）—— `hurt` 里有没有无条件 `return false`；死亡演出能不能恢复；`/kill` 是否有效。
10. **渲染兼容**（§8）—— Sodium 路径是否覆盖；光影检测是否做了；自定义 GUI 的背景顺序（§8.5）。
11. **语言/资源**（§9）—— 4 处齐全。
12. ⭐ **无窗口自测**（§11）—— 至少用 RCON 跑一遍：开世界 → 生成/伤害/拾取 → 存档卸载 → 读档。
13. **回滚与备份**（§10）—— 每一步都保证能退。
14. **发布与 CI**（§17）—— jar + SHA256 + Release + Actions。

---

## 20. 五条"别做"的事

1. **不要为了"看起来能过"而 try/catch 吞异常** —— 本项目多次靠"日志干净"误判成"没问题"，实际是**根本没执行**（死方法）或**被吞了**。
2. **不要靠记忆写 API** —— 用 `javap` / 源码 jar。每次"凭记忆"都要返工。
3. **不要把 `forge:` 的标签机械改成 `c:`**，也不要在 `c:` 里发明不存在的标签 —— 会同时破坏约定和第三方兼容。
4. 🆕 **不要用"逐行一致"给移植代码背书** —— 1.21 改了很多**语义**（§3.6、§8.5、§12.1、§12.5），照抄反而错。
5. 🆕 **不要在客户端验证** —— 能 `runServer` + 命令验证的，绝不开窗口；开窗口既慢又不可脚本化，还容易漏掉"存档后"的路径。

---

## 21. 🆕 验收清单（每完成一个阶段就勾一次）

> 原则：**每一项都要有"命令 + 输出"作为证据**，不接受"看起来对"。

### 构建与产物
- [ ] `gradlew build` 返回 `BUILD SUCCESSFUL`（不是 `UP-TO-DATE` 的旧产物；必要时 `clean build`）
- [ ] `build/libs/*.jar` 时间戳是本次构建，体积合理
- [ ] `unzip -l` / `javap` 抽查：本次新增的关键类**确实在 jar 里**
- [ ] `src/generated/resources` 已打包（`assets/<ns>/lang`、`models/item`、`particles` 都能在 jar 里找到）

### 数据侧
- [ ] `data/<ns>/` 一级/二级目录全是单数（§1.1），jar 内无复数残留
- [ ] 与上游官方发布包 diff：**文件数一致**，缺失的标签/资源已补（§2.3）
- [ ] jar 内所有 `*.json` 能被 `json.loads` 解析（0 语法错误）
- [ ] 每条配方的产出 id 真实存在；`result` 是 `{id: …}` 而不是裸字符串（§4.1）
- [ ] 战利品表里带附魔条件的条目，结构和原版同类型文件**逐键一致**（§4.5）
- [ ] 结构 `.nbt` 调色板里只剩自己的/`minecraft:` 命名空间（§13.1）

### 代码侧
- [ ] 所有覆写方法都有 `@Override`，且 `build` 通过（§3.3）
- [ ] grep 过 `getEffect() != ModEffects.*` / `== this` 这类 `Holder` 身份比较（§3.6）
- [ ] 所有 `hurt()` 覆盖里，`BYPASSES_INVULNERABILITY` 都能过（§12.1）
- [ ] 所有实体类型都有属性注册；`ATTACK_DAMAGE` / `MAX_HEALTH` 非 0（§12.4）
- [ ] 所有"死亡演出/假死"类状态都能在存档→读档后继续或安全收尾（§12.3）
- [ ] 所有 `ItemStack.save`/`ItemStack.CODEC` 路径都不会遇到空栈（§6.5）
- [ ] 所有 `AbstractArrow` 子类的 `getDefaultPickupItem()` 非空，且 `playerTouch` 处理了 `CREATIVE_ONLY`（§12.5）
- [ ] 可选依赖全部隔离，未安装时启动不报 `NoClassDefFoundError`（§14）
- [ ] 客户端 mixin 在独立配置里、`required:false` + `defaultRequire:0`（§15.2）

### 运行时（无窗口）
- [ ] `runServer` 出现 `Done (x.xxxs)!`，日志 0 条 `ERROR`
- [ ] 生成/伤害/拾取/存档卸载/读档 这条链路至少跑过一次（§11）
- [ ] 结构用 `place template` 放出来，方块数与 NBT 统计一致（§13.3）
- [ ] 读档后没有"0 血还活着 / 无敌 / NoAI 僵住"的实体（§12）

### 交付
- [ ] `dist/<mod>-<ver>.jar` + `dist/SHA256SUMS.txt`
- [ ] Release 附件大小/哈希与本地 `dist/` 一致
- [ ] CI 两个 job（构建 + 数据校验）全绿（§17.2）
- [ ] `PORTING-REPORT.md` / `NOTICE.md` 记录了"非官方移植、已知限制、与上游的差异"

---

## 22. 🆕 交接提示词模板（直接粘给下一个会话）

```text
帮我把 <模组名> 从 Forge 1.20.1 移植到 NeoForge 1.21.1。
- 上游源码：<本地路径或 GitHub 链接>（分支 <branch>）
- 目标工程：<新建目录>，NeoForge <21.1.x> + MDG <2.x> + JDK 21
- 构建环境：GRADLE_USER_HOME=<...>；JAVA_HOME=<...>；镜像/代理：<...>
- 要求：
  1. 严格按《Forge1.20.1_到_NeoForge1.21.1_移植经验.md》执行，先读"这份文档怎么用"和 §19 的顺序。
  2. **不要打开 Minecraft 客户端**（用户机器上不允许弹窗），所有验证用 runServer + RCON（文档 §11）。
  3. 每个修复都必须给出"命令 + 输出"作为证据，最后按文档 §21 的验收清单逐项报告。
  4. 数据包目录、标签、配方/战利品格式务必按 §1/§2/§4 体检，不要只求编译通过。
  5. 遇到"静默失效"先查 §3（死方法/`Holder` 比较），再查数据文件是否真的搬过来了。
  6. 交付：jar + SHA256 + 移植报告（含已知限制、与上游的差异、每个坑的根因）。
- 已知要注意的点（可选）：<比如：有可选依赖、有自定义结构、有自绘 GUI、有自定义实体>
```

### 给"下一个会话"的三个提醒

1. **先建"无窗口验证"能力，再改代码**。先把 `runServer` + RCON 跑通、把临时调试命令的骨架搭好（§11），
   后面每个 bug 都能 5 分钟内复现/验证，否则会陷入"改一版、让用户开游戏试一版"的循环。
2. **不要相信"编译通过"**。本项目的每一类致命 bug（死方法、`Holder` 比较、空栈、假死僵住、
   结构空洞、配方静默失效）都是在**编译完全通过**的前提下发生的。
3. **改动要能回滚**。每改一批就 `git commit` 一次，commit message 写清"根因 + 修法 + 验证方式"，
   这样下一个会话（或未来的你）能看懂为什么改。

---

*文档结束。如果还有"某个功能迁移后静默失效"的问题，先按 §18 的速查表对一遍，大概率能直接命中。*
