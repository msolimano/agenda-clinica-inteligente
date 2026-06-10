import type { BIBarItem } from './biDashboard.types';

interface SimpleBarListProps {
  items: BIBarItem[];
  emptyLabel: string;
}

export function SimpleBarList({ items, emptyLabel }: SimpleBarListProps) {
  const maxValue = Math.max(...items.map((item) => item.value), 0);

  if (!items.length) {
    return <p className="bi-simple-list__empty">{emptyLabel}</p>;
  }

  return (
    <div className="bi-simple-list">
      {items.map((item) => {
        const width = maxValue ? Math.max(8, Math.round((item.value / maxValue) * 100)) : 0;
        return (
          <div className="bi-simple-list__item" key={item.label}>
            <div className="bi-simple-list__row">
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </div>
            <div className="bi-simple-list__track" aria-hidden="true">
              <span style={{ width: `${width}%` }} />
            </div>
          </div>
        );
      })}
    </div>
  );
}
