const report = window.__MILKYWAY_REPORT__;

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

const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: report.elements,
    layout: {
        name: "preset"
    },
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
            selector: ".selectedNode",
            style: {
                "border-width": 4,
                "border-color": "#22c55e"
            }
        }
    ]
});

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

        updateRenderTime(renderStartedAt);
    });

    layout.run();
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
    cy.elements().removeClass("selectedNode");

    if (!restoreBasePositions()) {
        buildInitialLayout();
        return;
    }

    applyCriticalPathVisibility();
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

cy.on("tap", "node", event => {
    const node = event.target;

    cy.nodes().removeClass("selectedNode");
    node.addClass("selectedNode");
});

cy.on("pan zoom render", () => {
    updateGroupOverlays();
});

cy.ready(() => {
    buildInitialLayout();
});

window.addEventListener("resize", () => {
    fitStable();
    updateGroupOverlays();
});
