# Plan: Klay re-layout after group expand

## Context

После expand группы дочерние узлы появляются в тех позициях, где были до collapse — они могут перекрываться друг с другом и со внешними узлами, потому что `layoutBy: null` и `fisheye: false` явно отключают любой автоматический relayout. Нужно автоматически перекладывать только потомков раскрытой группы через klay, не трогая остальной граф.

## Изменяемый файл

`features/visualizer/cytoscape/impl/src/main/resources/web/cytoscape-view.js`

## Что делаем

Добавить обработчик события `expandcollapse.afterExpand` сразу после блока инициализации `ec` (строка ~276):

```javascript
cy.on('expandcollapse.afterExpand', (event) => {
    const expanded = event.target;
    const children = expanded.descendants();

    children.layout({
        name: 'klay',
        klay: { direction: 'RIGHT', spacing: 15 },
        animate: false,
        fit: false,
        padding: 20
    }).run();
});
```

**`fit: false`** — обязателен, чтобы viewport не прыгал на подграф.  
**`descendants()`** — берёт всех потомков рекурсивно, нужно для вложенных групп.  
**`spacing: 15`** — то же значение, что у кнопки "Klay layout".

## Исправление: учёт поворота графа

`rotateGraph` вращает все узлы вокруг центра bounding box всего графа. После klay дети встают в `direction: 'RIGHT'`, не совпадая с текущим углом.

**Фикс:** добавить переменную `currentRotation` и применять накопленный угол к детям после klay — вокруг центра **всего** графа, чтобы новые позиции вошли в ту же систему координат.

```javascript
let currentRotation = 0; // добавить рядом с basePositions

// в rotateGraph — добавить одну строку после объявления radians:
currentRotation = (currentRotation + degrees) % 360;

// в expandcollapse.afterExpand — после children.layout(...).run():
if (currentRotation !== 0) {
    const radians = currentRotation * Math.PI / 180;
    const cos = Math.cos(radians);
    const sin = Math.sin(radians);
    const box = cy.elements().boundingBox();
    const centerX = (box.x1 + box.x2) / 2;
    const centerY = (box.y1 + box.y2) / 2;

    children.positions(node => {
        const p = node.position();
        const dx = p.x - centerX;
        const dy = p.y - centerY;
        return {
            x: centerX + dx * cos - dy * sin,
            y: centerY + dx * sin + dy * cos
        };
    });
}
```

## Что НЕ меняем

- Инициализацию `ec` (`layoutBy: null`, `fisheye: false`) — они управляют collapse-поведением, не expand-событием.
- `saveBasePositions()` — не вызываем после expand-relayout, т.к. `resetGraph()` логично возвращать к начальному состоянию, а не к промежуточным expand-позициям.
