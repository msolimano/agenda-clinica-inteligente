interface SimpleStatusDistributionItem {
  label: string;
  value: number;
  color: string;
}

interface SimpleStatusDistributionProps {
  items: SimpleStatusDistributionItem[];
  emptyLabel: string;
}

export function SimpleStatusDistribution({ items, emptyLabel }: SimpleStatusDistributionProps) {
  const total = items.reduce((sum, item) => sum + item.value, 0);

  if (total === 0) {
    return <p className="bi-chart-empty">{emptyLabel}</p>;
  }

  let cursor = 0;
  const gradient = items.map((item) => {
    const start = cursor;
    const end = cursor + (item.value / total) * 360;
    cursor = end;
    return `${item.color} ${start}deg ${end}deg`;
  }).join(', ');

  return (
    <div className="bi-status-distribution">
      <div className="bi-status-distribution__donut" style={{ background: `conic-gradient(${gradient})` }}>
        <span>{total}</span>
        <small>Total</small>
      </div>
      <div className="bi-status-distribution__legend">
        {items.map((item) => (
          <div className="bi-status-distribution__item" key={item.label}>
            <span><i style={{ backgroundColor: item.color }} />{item.label}</span>
            <strong>{item.value}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}
