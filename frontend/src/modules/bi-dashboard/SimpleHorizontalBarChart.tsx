export interface SimpleHorizontalBarItem {
  label: string;
  value: number;
  detail?: string;
}

interface SimpleHorizontalBarChartProps {
  items: SimpleHorizontalBarItem[];
  emptyLabel: string;
  valueFormatter?: (value: number) => string;
}

export function SimpleHorizontalBarChart({ items, emptyLabel, valueFormatter = String }: SimpleHorizontalBarChartProps) {
  const maxValue = Math.max(...items.map((item) => item.value), 0);

  if (!items.length || maxValue === 0) {
    return <p className="bi-chart-empty">{emptyLabel}</p>;
  }

  return (
    <div className="bi-horizontal-chart">
      {items.map((item) => {
        const width = Math.max(8, Math.round((item.value / maxValue) * 100));
        return (
          <div className="bi-horizontal-chart__item" key={item.label}>
            <div className="bi-horizontal-chart__row">
              <span>{item.label}</span>
              <strong>{valueFormatter(item.value)}</strong>
            </div>
            <div className="bi-horizontal-chart__track" aria-hidden="true">
              <span style={{ width: `${width}%` }} />
            </div>
            {item.detail ? <small>{item.detail}</small> : null}
          </div>
        );
      })}
    </div>
  );
}
