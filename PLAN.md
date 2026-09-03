# Plan: Recursive grouping hierarchy

## Context

Currently `groupIdOf(node)` returns only `substringBefore(":")` — one flat group per first
path component. Modules like `feature:auth:common` and `feature:auth:impl` land in the same
`feature` bucket; intermediate group `feature:auth` is never created. The goal is full
recursive hierarchy: `feature:auth:common → feature:auth → feature`, with compound nodes
nested in Cytoscape and overlay boxes at every level (visual depth distinction via border
style and opacity).

---

## Critical files

| File | What changes |
|---|---|
| `features/visualizer/cytoscape/api/src/main/kotlin/com/github/milkyway/visualizer/cytoscape/Cytoscape.kt` | Add `parent: String? = null` to `CytoscapeGroupDto` |
| `milkyway-plugin/core/src/main/kotlin/com/github/milkyway/core/models/Cytoscape.kt` | Same — parallel copy of DTOs still used by `ReportBuilder` |
| `milkyway-plugin/idea-plugin/src/main/kotlin/com/github/milkyway/idea/cytoscape/ReportBuilder.kt` | Replace flat group logic with recursive version |
| `features/visualizer/cytoscape/impl/src/main/kotlin/com/github/milkyway/visualizer/cytoscape/CytoscapeVisualizer.kt` | Same rewrite if this path is also active |
| `features/visualizer/cytoscape/impl/src/main/resources/web/cytoscape-view.js` | Typedef update + depth-aware overlay rendering |

---

## Step 1 — Add `parent` to `CytoscapeGroupDto` (both DTO files)

```kotlin
@Serializable
data class CytoscapeGroupDto(
    val id: String,
    val label: String,
    val nodes: List<String>,
    val parent: String? = null,   // ← ADD
)
```

---

## Step 2 — Replace group-building logic in `ReportBuilder.buildCytoscapeReport`

Remove `groupIdOf`. Add two private helpers:

```kotlin
// All ancestor group IDs, root-first:
// "feature:auth:common" → ["feature", "feature:auth"]
private fun ancestorGroupIds(nodeId: String): List<String> {
    val parts = nodeId.split(":")
    return (1 until parts.size).map { parts.take(it).joinToString(":") }
}

// Immediate parent group ID, or null if root node:
// "feature:auth:common" → "feature:auth"
// "app"                 → null
private fun immediateParentOf(id: String): String? {
    val i = id.lastIndexOf(":")
    return if (i > 0) id.substring(0, i) else null
}
```

Replace the current groups/groupElements/nodes blocks:

```kotlin
// All unique intermediate group IDs across all modules
val allGroupIds = modules
    .flatMap { ancestorGroupIds(it.id) }
    .distinct()
    .sorted()

val groups = allGroupIds.map { groupId ->
    // direct children = leaf nodes + sub-groups whose immediate parent is this group
    val directLeaves    = modules.filter { immediateParentOf(it.id) == groupId }.map { it.id }
    val directSubGroups = allGroupIds.filter { immediateParentOf(it) == groupId }
    CytoscapeGroupDto(
        id     = groupId,
        label  = groupId.substringAfterLast(":"),
        nodes  = (directLeaves + directSubGroups).sorted(),
        parent = immediateParentOf(groupId),
    )
}

val groupElements = groups.map { group ->
    CytoscapeElementDto(
        data = CytoscapeDataDto(
            id     = group.id,
            label  = group.label,
            parent = group.parent,     // ← compound nesting comes from here
        ),
        classes = "groupNode"
    )
}

val nodes = modules.map { node ->
    val isCritical = node in criticalNodes
    val isAP       = node in articulationPointsResult
    CytoscapeElementDto(
        data = CytoscapeDataDto(
            id                  = node.id,
            label               = node.label,
            parent              = immediateParentOf(node.id),  // immediate group, not root
            critical            = isCritical,
            isArticulationPoint = isAP,
        ),
        classes = if (isCritical) "critical" else "",
    )
}
```

> **Edge case**: modules with no `:` (e.g. `app`) — `ancestorGroupIds` returns `[]`,
> `immediateParentOf` returns `null`. These become root-level leaf nodes with no compound
> parent, which is correct.

Apply the same rewrite to `CytoscapeVisualizer.kt` in features if it is the active render path.

---

## Step 3 — JS: typedef + depth-aware overlay

### 3a. Update `@typedef CytoscapeGroupDto` (line 15–19)

```js
/**
 * @typedef CytoscapeGroupDto
 * @property {String} id
 * @property {String} label
 * @property {String[]} nodes
 * @property {?String} parent
 */
```

### 3b. Add depth helper before `updateGroupOverlays`

```js
function groupDepth(group, allGroups) {
    let depth = 0;
    let cur = group;
    while (cur.parent) {
        cur = allGroups.find(g => g.id === cur.parent);
        if (!cur) break;
        depth++;
    }
    return depth;
}
```

### 3c. Apply depth styling inside `updateGroupOverlays` forEach

Bounding-box calculation is unchanged — `cy.getElementById(childId).renderedBoundingBox()`
works correctly for both leaf nodes and compound (sub-group) nodes.

**Alpha algorithm**: deepest group = 100%, each level up loses `100 / (maxDepth + 1)` %.
Formula: `alpha = (depth + 1) / (maxDepth + 1)`.

Example — 3 levels (depths 0, 1, 2), step = 1/3 ≈ 0.33:
- depth 0 (root): alpha ≈ 0.33
- depth 1:        alpha ≈ 0.67
- depth 2 (leaf group): alpha = 1.0

```js
const groups = report.groups || [];
const maxDepth = groups.length > 0
    ? Math.max(...groups.map(g => groupDepth(g, groups)))
    : 0;

groups.forEach(group => {
    // ... bounding box calculation unchanged ...

    const depth = groupDepth(group, groups);
    const alpha = (depth + 1) / (maxDepth + 1);

    const groupBox = document.createElement("div");
    groupBox.className = "groupBox";
    // ... position styles ...
    groupBox.style.borderColor = `rgba(180, 180, 180, ${alpha.toFixed(2)})`;
    groupBox.style.zIndex = String(depth);
    // ...
})
```

---

## Collapse on load — no JS change needed

`ec.collapseAll()` already collapses all compound nodes recursively. With nested groups,
expanding a root group reveals collapsed sub-groups; expanding a sub-group reveals leaves.
This is the correct "all levels" behaviour without any code change.

---

## Verification

1. `make dev` — launch sandbox IDE.
2. Open a multi-level project (e.g. `test/test-project/` with `feature:X:Y` modules).
3. Check graph loads with compound hierarchy: root groups visible, cue buttons show +/−.
4. Enable "Enable grouping on load" — verify graph collapses to root groups only.
5. Click +/− to expand a root group — sub-groups appear collapsed.
6. Enable "Group overlay" checkbox — verify overlay boxes appear at every level with
   distinct border/opacity per depth.
7. Drag a deep-level group overlay — verify child nodes move correctly.
