import type { BITrendPoint } from './biDashboard.types';

type TrendMetricKey = 'scheduled' | 'confirmed' | 'cancelled' | 'noShow';

interface TrendSeries {
  key: TrendMetricKey;
  label: string;
  color: string;
}

interface SimpleTrendChartProps {
  points: BITrendPoint[];
  series: TrendSeries[];
  emptyLabel: string;
}

const WIDTH = 640;
const HEIGHT = 250;
const PADDING = { top: 24, right: 22, bottom: 44, left: 38 };

export function SimpleTrendChart({ points, series, emptyLabel }: SimpleTrendChartProps) {
  const maxValue = Math.max(...points.flatMap((point) => series.map((item) => point[item.key])), 0);

  if (!points.length || maxValue === 0) {
    return <p className="bi-chart-empty">{emptyLabel}</p>;
  }

  const plotWidth = WIDTH - PADDING.left - PADDING.right;
  const plotHeight = HEIGHT - PADDING.top - PADDING.bottom;
  const xFor = (index: number) => PADDING.left + (points.length === 1 ? plotWidth / 2 : (index / (points.length - 1)) * plotWidth);
  const yFor = (value: number) => PADDING.top + plotHeight - (value / maxValue) * plotHeight;
  const firstDate = formatDate(points[0].date);
  const lastDate = formatDate(points[points.length - 1].date);

  return (
    <div className="bi-trend-chart">
      <svg className="bi-trend-chart__canvas" viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label="Tendencia diaria de citas">
        <line className="bi-trend-chart__axis" x1={PADDING.left} x2={WIDTH - PADDING.right} y1={PADDING.top + plotHeight} y2={PADDING.top + plotHeight} />
        <line className="bi-trend-chart__axis" x1={PADDING.left} x2={PADDING.left} y1={PADDING.top} y2={PADDING.top + plotHeight} />
        {[0.25, 0.5, 0.75].map((ratio) => {
          const y = PADDING.top + plotHeight * ratio;
          return <line className="bi-trend-chart__grid" key={ratio} x1={PADDING.left} x2={WIDTH - PADDING.right} y1={y} y2={y} />;
        })}
        <text className="bi-trend-chart__label" x={PADDING.left - 8} y={PADDING.top + 4} textAnchor="end">{maxValue}</text>
        <text className="bi-trend-chart__label" x={PADDING.left - 8} y={PADDING.top + plotHeight + 4} textAnchor="end">0</text>
        <text className="bi-trend-chart__label" x={PADDING.left} y={HEIGHT - 12}>{firstDate}</text>
        <text className="bi-trend-chart__label" x={WIDTH - PADDING.right} y={HEIGHT - 12} textAnchor="end">{lastDate}</text>
        {series.map((item) => {
          const linePoints = points.map((point, index) => `${xFor(index)},${yFor(point[item.key])}`).join(' ');
          return <polyline className="bi-trend-chart__line" key={item.key} points={linePoints} stroke={item.color} />;
        })}
      </svg>
      <div className="bi-trend-chart__legend">
        {series.map((item) => <span key={item.key}><i style={{ backgroundColor: item.color }} />{item.label}</span>)}
      </div>
    </div>
  );
}

function formatDate(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return new Intl.DateTimeFormat('es-CL', { day: '2-digit', month: 'short' }).format(date);
}
