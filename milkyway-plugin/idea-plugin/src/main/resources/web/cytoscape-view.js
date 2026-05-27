/**
 * @typedef CytoscapePluginSettingsDto
 * @property {Boolean} isAnimationEnabled
 * @property {String} theme
 * @property {Boolean} isWebGlEnabled
 * @property {Boolean} isGroupOnLoadEnabled
 */
/**
 * @typedef CytoscapeShapeSimilarityDto
 * @property {String} shapeId
 * @property {String} shapeName
 * @property {Number} similarityPercent
 */
/**
 * @typedef CytoscapeGroupDto
 * @property {String} id
 * @property {String} label
 * @property {String[]} nodes
 */
/**
 * @typedef CytoscapeDataDto
 * @property {String} id
 * @property {?String} label
 * @property {?String} group
 * @property {?String} parent
 * @property {?String} source
 * @property {?String} target
 * @property {Boolean} critical
 * @property {Boolean} isArticulationPoint
 */
/**
 * @typedef CytoscapeElementDto
 * @property {CytoscapeDataDto} data
 * @property {String} classes
 */
/**
 * @typedef CytoscapeSummaryDto
 * @property {Number} nodeCount
 * @property {Number} edgeCount
 * @property {Number} criticalPathLength
 */
/**
 * @typedef CytoscapeReportDto
 * @property {CytoscapeSummaryDto} summary
 * @property {CytoscapeElementDto[]} elements
 * @property {String[][]} criticalPaths
 * @property {CytoscapeGroupDto[]} groups
 * @property {CytoscapeShapeSimilarityDto[]} shapeSimilarities
 * @property {CytoscapePluginSettingsDto} cytoscapePluginSettings
 */

/** @type {CytoscapeReportDto} report */
const report = window.__MILKYWAY_REPORT__;
const pluginSettings = report.cytoscapePluginSettings;
console.log({'pluginSettings': pluginSettings});

const summary = report.summary || {};
document.getElementById("nodeCount").innerText = summary.nodeCount ?? "–";
document.getElementById("edgeCount").innerText = summary.edgeCount ?? "–";
document.getElementById("criticalPathLength").innerText = summary.criticalPathLength ?? "–";

const FIT_PADDING = 80;

let basePositions = null;
let draggedGroup = null;

const layoutOptions = {
    name: "breadthfirst",
    directed: true,
    padding: 30,
    spacingFactor: 1.7,
    avoidOverlap: true,
    nodeDimensionsIncludeLabels: false,
    fit: false,
    animate: false
};

let renderer = {}
if (pluginSettings.isWebGlEnabled) {
    /**
     * {@link https://blog.js.cytoscape.org/2025/01/13/webgl-preview/}
     */
    renderer = {
        name: 'canvas',  // still uses the canvas renderer
            webgl: true, // turns on WebGL mode
            showFps: true,
            webglDebug: true, // (optional) prints debug info to the browser console

            webglTexSize: 4096,
            webglTexRows: 24,
            webglBatchSize: 2048,
            webglTexPerBatch: 16,
    }
}

const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: report.elements,
    layout: {
        name: "preset"
    },
    renderer: renderer,
    selectionType: "additive",
    style: [
        {
            selector: "node",
            style: {
                "label": "data(label)",
                "font-size": 6,
                "text-wrap": "wrap",
                "text-max-width": 90,
                "background-color": "#7aa2f7",
                "border-width": 1,
                "border-color": "#333",
                "color": "#eaeaea",
                "text-outline-width": 2,
                "text-outline-color": "#1e1e1e"
            }
        },
        {
            selector: "edge",
            style: {
                "curve-style": "bezier",
                "target-arrow-shape": "triangle",
                "width": 1,
                "arrow-scale": 0.8,
                "line-color": "#8a8a8a",
                "target-arrow-color": "#8a8a8a"
            }
        },
        {
            selector: 'edge.cy-expand-collapse-collapsed-edge',
            style:
                {
                    "text-outline-color": "#ffffff",
                    "text-outline-width": "2px",
                    'label': (e) => {
                        return '(' + e.data('collapsedEdges').length + ')';
                    },
                    'line-style': 'dashed',
                }
        },
        {
            selector: ".articulationPointHighlight",
            style: {
                "background-color": "#22c55e",
                "border-width": 3,
                "border-color": "#166534"
            }
        },
        {
            selector: ".groupNode",
            style: {
                "background-opacity": 0.05,
                "border-width": 2,
                "border-style": "dotted",
                "border-color": "#888",
                "label": "data(label)",
                "font-size": 10,
                "color": "#cccccc",
                "text-valign": "top",
                "text-halign": "center",
                "padding": 20
            }
        },
        {
            selector: ".criticalHighlight",
            style: {
                "background-color": "#f7768e",
                "border-width": 3,
                "border-color": "#9d174d"
            }
        },
        {
            selector: ".criticalEdgeHighlight",
            style: {
                "width": 3,
                "line-color": "#f7768e",
                "arrow-scale": 0.8,
                "target-arrow-color": "#f7768e"
            }
        },
        {
            selector: "node:selected",
            style: {
                "border-width": 4,
                "border-color": "#22c55e"
            }
        }
    ]
});

// const ur = cy.undoRedo();
// console.log({
//     'ur': ur,
// })

function buildInitialLayout() {
    const layout = cy.layout(layoutOptions);
    const renderStartedAt = performance.now();

    layout.on("layoutstop", () => {
        orientRootsLeft();
        normalizeGraphAspect();
        saveBasePositions();
        applyCriticalPathVisibility();
        tuneLabelScale();
        fitStable();
        updateGroupOverlays();
        applyArticulationPointVisibility();
        renderShapeSimilarityLegend();

        updateRenderTime(renderStartedAt);
        if (pluginSettings.isGroupOnLoadEnabled) {
            mcollapseAllNodes();
            mcollapseAllEdges();
        }
        console.log("Graph is initialized");
    });

    layout.run();
}

// region Grouping Nodes
const ec = cy.expandCollapse({
    // To prevent from relayout after expand/collapse.
    // E.g. it rotates 90 degrees
    layoutBy: null,
    // To prevent from moving nodes on expand.
    // Change true to see effect
    fisheye: false,
    animate: false,
    // To show system +- expand/collapse buttons
    cueEnabled: true
});
console.log({
    'ec': ec,
});
function applyArticulationPointVisibility() {
    const enabled = document.getElementById("articulationPointsCheckbox").checked;

    cy.nodes().removeClass("articulationPointHighlight");

    if (!enabled) {
        return;
    }

    cy.nodes()
        .filter(node => node.data("isArticulationPoint") === true)
        .addClass("articulationPointHighlight");
}

function getNewGroupIdCallback() {
    let id = 1;
    return function() {
        return id++;
    }
}
const getNewGroupId = getNewGroupIdCallback();

function addParentNode(idSuffix, parent = undefined) {
    const id = 'c' + idSuffix;
    const parentNode = {
        data: {
            id: id,
            label: `group[${idSuffix}]`
        },
        classes: 'groupNode',
    };
    cy.add(parentNode);
    cy.$('#' + id).move({parent: parent});
    return id;
}

function addCompound() {
    const selectedElements = cy.elements(':selected');
    if (selectedElements.length < 1) {
        console.warn('No selected elements!', selectedElements);
        return;
    }
    const parent = selectedElements[0].parent().id();
    const nodesWithDifferentParent = selectedElements.filter(
        selectedElement => selectedElement.parent().id() !== parent
    );
    const hasDifferentParent = nodesWithDifferentParent.nonempty();
    console.log({
        'parent': parent,
        'nodeWithDifferentParent': nodesWithDifferentParent,
        'hasDifferentParent': hasDifferentParent,
    });
    if (hasDifferentParent) {
        console.warn('Selected nodes have different parent!')
        return;
    }
    const id = getNewGroupId();
    addParentNode(id, parent);
    selectedElements.forEach(selectedElement => {
        selectedElement.move({parent: 'c' + id})
    });
}

function collapseSelected() {
    const selectedNodes = cy.nodes(':selected');
    const selectedEdges = cy.edges(':selected')

    const typeIds = cy.elements(':selected')
    const nodeIds = cy.elements('node:selected')
    const edgeIds = cy.elements('edge:selected');

    console.log({
        'typeIds': typeIds,
        'nodeIds': nodeIds,
        'edgeIds': edgeIds,
    });

    console.log({
        'selectedNodes': selectedNodes,
        'selectedEdges': selectedEdges,
    })

    if (selectedNodes.length > 0) {
        ec.collapseRecursively(selectedNodes);
    }
    if (selectedEdges.length > 0) {
        ec.collapseRecursively(selectedEdges);
    }
}

function expandSelected() {
    const selectedNodes = cy.nodes(':selected')
    const selectedEdges = cy.edges(':selected')

    console.log({
        'selectedNodes': selectedNodes,
        'selectedEdges': selectedEdges,
    });
    if (selectedNodes.length > 0) {
        ec.expandRecursively(selectedNodes);
    }
    if (selectedEdges.length > 0) {
        ec.expandRecursively(selectedEdges);
    }
}

function collapseEdgesBetweenNodes() {
    ec.collapseEdgesBetweenNodes(cy.nodes(':selected'), {
        groupEdgesOfSameTypeOnCollapse: false,
        allowNestedEdgeCollapse: true,
    });
}

function expandEdgesBetweenNodes() {
    ec.expandEdgesBetweenNodes(cy.nodes(':selected'), {
        groupEdgesOfSameTypeOnCollapse: false,
        allowNestedEdgeCollapse: true,
    });
}

function mcollapseAllNodes() {
    ec.collapseAll();
}

function mexpandAllNodes() {
    ec.expandAll();
}

function mcollapseAllEdges() {
    ec.collapseAllEdges({
        groupEdgesOfSameTypeOnCollapse: false,
        allowNestedEdgeCollapse: true,
    })
}

document.addEventListener('keydown', event => {
    const isCtrl = event.ctrlKey || event.metaKey;
    if (!isCtrl || !event.shiftKey) {
        return;
    }

    const expandKeys = [
        'Equal',
        'NumpadAdd',
        'Slash'
    ];
    const collapseKeys = [
        'Minus',
        'NumpadSubtract',
        'KeyK'
    ];
    const isExpandAll = expandKeys.includes(event.code);
    const isCollapseAll = collapseKeys.includes(event.code);

    console.log({'event.code': event.code});
    if (isExpandAll || isCollapseAll) {
        event.preventDefault();
    } else {
        return;
    }

    if (isExpandAll) {
        mexpandAllNodes();
    } else if (isCollapseAll) {
        mcollapseAllNodes();
    }
});


cy.on('tap', 'node, edge', event => {
    const originalEvent = event.originalEvent;
    const element = event.target;

    const isCtrl =
        originalEvent.ctrlKey ||
        originalEvent.metaKey;

    if (isCtrl) {
        element.selected(!element.selected());
    } else {
        cy.elements().unselect();
        element.selected(true);
    }
});
// endregion

// region Zoom
let isZoomAnimating = false;
/**
 * @param {Number} factor
 */
function zoomBy(factor) {
    if (isZoomAnimating) {
        return;
    }
    const centerX = cy.width() / 2;
    const centerY = cy.width() / 2;
    const isAnimationEnabled = pluginSettings.isAnimationEnabled;

    const zoomOptions = {
        level: cy.zoom() * factor,
        renderedPosition: {
            x: centerX,
            y: centerY,
        }
    }
    if (isAnimationEnabled) {
        isZoomAnimating = true;
        cy.animate({zoom: zoomOptions}, {
            duration: 200,
            complete: () => { isZoomAnimating = false; },
        })
    } else {
        cy.zoom(zoomOptions);
    }
}

/**
 * @param {Number} factor
 */
function zoomIn(factor) {
    zoomBy(factor)
}

/**
 * @param {Number} factor
 */
function zoomOut(factor) {
    zoomBy(1 / factor)
}

function fitGraph() {
    const isAnimationEnabled = pluginSettings.isAnimationEnabled;
    if (isAnimationEnabled) {
        cy.animate({
            fit: {
                eles: cy.elements(),
                padding: FIT_PADDING
            }
        }, {
            duration: 300
        });
    } else {
        cy.fit();
    }
}

document.addEventListener('keydown', event => {
    const isCtrl = event.ctrlKey || event.metaKey;
    const zoomFactor = 1.2;
    if (!isCtrl) {
        return;
    }

    const zoomInKeys = [
        'Equal',
        'NumpadAdd',
        'Slash'
    ];
    const zoomOutKeys = [
        'Minus',
        'NumpadSubtract',
        'KeyK'
    ];
    const isZoomIn = zoomInKeys.includes(event.code);
    const isZoomOut = zoomOutKeys.includes(event.code);

    console.log({'event.code': event.code});
    if (isZoomIn || isZoomOut) {
        event.preventDefault();
    } else {
        return;
    }

    if (isZoomIn) {
        zoomIn(zoomFactor);
    } else if (isZoomOut) {
        zoomOut(zoomFactor);
    }
});
// endregion

function renderShapeSimilarityLegend() {
    const container = document.getElementById("shapeSimilarityLegendBody");
    if (container === null) return;

    const similarities = report.shapeSimilarities || [];
    container.innerHTML = "";

    if (similarities.length === 0) {
        const empty = document.createElement("div");
        empty.className = "legendRow";
        empty.innerHTML = '<span class="legendLabel">None</span>';
        container.appendChild(empty);
        return;
    }

    similarities.forEach(item => {
        const row = document.createElement("div");
        row.className = "legendRow";

        const label = document.createElement("span");
        label.className = "legendLabel";
        label.innerText = item.shapeName || item.shapeId;

        const value = document.createElement("span");
        value.className = "legendValue";
        value.innerText = `${item.similarityPercent.toFixed(0)}%`;

        row.appendChild(label);
        row.appendChild(value);
        container.appendChild(row);
    });
}

function updateRenderTime(renderStartedAt) {
    const renderTime = Math.round(performance.now() - renderStartedAt);
    const element = document.getElementById("renderTime");

    if (element !== null) {
        element.innerText = renderTime.toString();
    }
}

function saveBasePositions() {
    basePositions = new Map();

    cy.nodes().forEach(node => {
        const position = node.position();

        basePositions.set(node.id(), {
            x: position.x,
            y: position.y
        });
    });
}

function restoreBasePositions() {
    if (basePositions === null) {
        return false;
    }

    cy.nodes().positions(node => {
        return basePositions.get(node.id()) || node.position();
    });

    return true;
}

function orientRootsLeft() {
    const box = cy.elements().boundingBox();

    cy.nodes().positions(node => {
        const position = node.position();

        return {
            x: position.y - box.y1 + box.x1,
            y: position.x - box.x1 + box.y1
        };
    });
}

function resetGraph() {
    cy.elements().unselect();

    if (!restoreBasePositions()) {
        buildInitialLayout();
        return;
    }

    applyCriticalPathVisibility();
    applyArticulationPointVisibility();
    tuneLabelScale();
    fitStable();
    updateGroupOverlays();
}

function rotateGraph(degrees) {
    const radians = degrees * Math.PI / 180;
    const cos = Math.cos(radians);
    const sin = Math.sin(radians);

    const box = cy.elements().boundingBox();
    const centerX = (box.x1 + box.x2) / 2;
    const centerY = (box.y1 + box.y2) / 2;

    cy.nodes().positions(node => {
        const position = node.position();
        const dx = position.x - centerX;
        const dy = position.y - centerY;

        return {
            x: centerX + dx * cos - dy * sin,
            y: centerY + dx * sin + dy * cos
        };
    });

    fitStable();
    updateGroupOverlays();
}

function fitStable() {
    cy.resize();
    cy.fit(cy.nodes(), FIT_PADDING);
    cy.center(cy.nodes());
}

function tuneLabelScale() {
    const nodeCount = cy.nodes().length;

    let fontSize = 9;
    let textMaxWidth = 110;

    if (nodeCount > 120) {
        fontSize = 6;
        textMaxWidth = 70;
    } else if (nodeCount > 80) {
        fontSize = 7;
        textMaxWidth = 80;
    } else if (nodeCount > 40) {
        fontSize = 8;
        textMaxWidth = 95;
    }

    cy.nodes().style({
        "font-size": fontSize,
        "text-max-width": textMaxWidth
    });
}

function applyCriticalPathVisibility() {
    const enabled = document.getElementById("criticalPathCheckbox").checked;

    cy.nodes().removeClass("criticalHighlight");
    cy.edges().removeClass("criticalEdgeHighlight");

    if (!enabled) {
        return;
    }

    cy.nodes()
        .filter(node => node.data("critical") === true)
        .addClass("criticalHighlight");

    cy.edges()
        .filter(edge => edge.data("critical") === true)
        .addClass("criticalEdgeHighlight");
}

function toggleLegend() {
    const body = document.getElementById("legendBody");
    const toggle = document.getElementById("legendToggle");

    const hidden = body.style.display === "none";

    if (hidden) {
        body.style.display = "block";
        toggle.innerText = "−";
    } else {
        body.style.display = "none";
        toggle.innerText = "+";
    }
}

function toggleShapeLegend() {
    const body = document.getElementById("shapeSimilarityLegendBody");
    const toggle = document.getElementById("legendShapeToggle");

    const hidden = body.style.display === "none";

    if (hidden) {
        body.style.display = "block";
        toggle.innerText = "−";
    } else {
        body.style.display = "none";
        toggle.innerText = "+";
    }
}

function normalizeGraphAspect() {
    const box = cy.nodes().boundingBox({
        includeLabels: false,
        includeOverlays: false
    });

    if (box.w === 0 || box.h === 0) {
        return;
    }

    const viewportAspect = cy.width() / cy.height();
    const graphAspect = box.w / box.h;

    const centerX = (box.x1 + box.x2) / 2;
    const centerY = (box.y1 + box.y2) / 2;

    let scaleX = 1;
    let scaleY = 1;

    if (graphAspect < viewportAspect) {
        scaleX = viewportAspect / graphAspect;
    } else {
        scaleY = graphAspect / viewportAspect;
    }

    scaleX = Math.min(scaleX, 2.5);
    scaleY = Math.min(scaleY, 2.5);

    cy.nodes().positions(node => {
        const position = node.position();

        return {
            x: centerX + (position.x - centerX) * scaleX,
            y: centerY + (position.y - centerY) * scaleY
        };
    });
}

const GROUP_PADDING = 24;

function applyGroupVisibility() {
    const enabled = document.getElementById("groupCheckbox").checked;

    if (!enabled) {
        clearGroupOverlays();
        return;
    }

    updateGroupOverlays();
}

function applyNodeLabelVisibility() {
    const enabled = document.getElementById("nodeLabelCheckbox").checked;

    if (!enabled) {
        clearNodeOverlays();
        return;
    }

    updateNodeOverlays();
}
// region Overlay
function clearGroupOverlays() {
    const overlay = document.getElementById("groupOverlay");
    overlay.innerHTML = "";
}

function updateGroupOverlays() {
    const checkbox = document.getElementById("groupCheckbox");

    if (checkbox === null || !checkbox.checked) {
        return;
    }

    const overlay = document.getElementById("groupOverlay");
    overlay.innerHTML = "";

    const groups = report.groups || [];

    groups.forEach(group => {
        const boxes = group.nodes
            .map(nodeId => cy.getElementById(nodeId))
            .filter(node => node.nonempty())
            .map(node => node.renderedBoundingBox({
                includeLabels: true,
                includeOverlays: false
            }));

        if (boxes.length === 0) {
            return;
        }

        const x1 = Math.min(...boxes.map(box => box.x1)) - GROUP_PADDING;
        const y1 = Math.min(...boxes.map(box => box.y1)) - GROUP_PADDING;
        const x2 = Math.max(...boxes.map(box => box.x2)) + GROUP_PADDING;
        const y2 = Math.max(...boxes.map(box => box.y2)) + GROUP_PADDING;

        const groupBox = document.createElement("div");
        groupBox.className = "groupBox";
        groupBox.dataset.groupId = group.id;
        groupBox.style.left = `${x1}px`;
        groupBox.style.top = `${y1}px`;
        groupBox.style.width = `${x2 - x1}px`;
        groupBox.style.height = `${y2 - y1}px`;

        groupBox.addEventListener("mousedown", event => {
            startGroupDrag(event, group);
        });

        const label = document.createElement("div");
        label.className = "groupBoxLabel";
        label.innerText = group.label || group.id;

        groupBox.appendChild(label);
        overlay.appendChild(groupBox);
    });
}

function clearNodeOverlays() {
    const nodeLabelOverlay = document.getElementById('nodeLabelOverlay');
    nodeLabelOverlay.innerText = '';
}

function updateNodeOverlays() {
    const checkboxEl = document.getElementById("nodeLabelCheckbox");
    if (!checkboxEl.checked) {
        return;
    }
    const overlayEl = document.getElementById('nodeLabelOverlay');
    overlayEl.innerHTML = '';

    let i = 0;
    const nodes = cy.nodes(':visible');
    console.log({'visible nodes': nodes});
    nodes.forEach(node => {
        if (node.isParent()) {
            return;
        }
        const {x, y} = node.renderedPosition()
        const nodeLabelEl = document.createElement('div');
        nodeLabelEl.className = 'groupBoxLabel';
        nodeLabelEl.style.left = `${x}px`;
        nodeLabelEl.style.top = `${y}px`;
        nodeLabelEl.innerText = node.data('label');
        overlayEl.appendChild(nodeLabelEl);
    })
}

function startGroupDrag(event, group) {
    event.preventDefault();
    event.stopPropagation();

    const nodePositions = new Map();

    group.nodes.forEach(nodeId => {
        const node = cy.getElementById(nodeId);

        if (node.nonempty()) {
            const position = node.position();

            nodePositions.set(nodeId, {
                x: position.x,
                y: position.y
            });
        }
    });

    if (nodePositions.size === 0) {
        return;
    }

    draggedGroup = {
        group,
        startClientX: event.clientX,
        startClientY: event.clientY,
        nodePositions
    };

    cy.userPanningEnabled(false);
    cy.userZoomingEnabled(false);

    document.addEventListener("mousemove", handleGroupDragMove);
    document.addEventListener("mouseup", stopGroupDrag);
}

function handleGroupDragMove(event) {
    if (draggedGroup === null) {
        return;
    }

    event.preventDefault();

    const dx = (event.clientX - draggedGroup.startClientX) / cy.zoom();
    const dy = (event.clientY - draggedGroup.startClientY) / cy.zoom();

    draggedGroup.nodePositions.forEach((startPosition, nodeId) => {
        const node = cy.getElementById(nodeId);

        if (node.nonempty()) {
            node.position({
                x: startPosition.x + dx,
                y: startPosition.y + dy
            });
        }
    });

    updateGroupOverlays();
}

function stopGroupDrag() {
    if (draggedGroup === null) {
        return;
    }

    draggedGroup = null;

    cy.userPanningEnabled(true);
    cy.userZoomingEnabled(true);

    document.removeEventListener("mousemove", handleGroupDragMove);
    document.removeEventListener("mouseup", stopGroupDrag);

    updateGroupOverlays();
}

cy.on("pan zoom render", () => {
    updateGroupOverlays();
    updateNodeOverlays();
});

cy.ready(() => {
    buildInitialLayout();
});

window.addEventListener("resize", () => {
    fitStable();
    updateGroupOverlays();
});

/**
 * @param {Number} spacing
 */
function applyKlayLayout(spacing) {
    cy.layout({
        name: 'klay',

        klay: {
            direction: 'RIGHT',
            spacing: spacing
        },

        animate: false,
        fit: true,
        padding: 40
    }).run();
}

// endregion
// region FPS
const fpsEl = document.getElementById('fpsCount')
let last = performance.now();
let frames = 0;

function tick(now) {
    ++frames;
    const elapsed = now - last;
    if (elapsed >= 1000) {
        fpsEl.textContent = (frames * 1000 / elapsed).toFixed(0);
        frames = 0;
        last = now;
    }
    requestAnimationFrame(tick);
}
requestAnimationFrame(tick);
// endregion