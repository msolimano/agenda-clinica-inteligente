import type { ReactNode } from 'react';

export interface RankingColumn<T> {
  header: string;
  align?: 'left' | 'right';
  render: (item: T, index: number) => ReactNode;
}

interface RankingTableProps<T> {
  items: T[];
  columns: RankingColumn<T>[];
  emptyLabel: string;
  getKey: (item: T) => string;
}

export function RankingTable<T>({ items, columns, emptyLabel, getKey }: RankingTableProps<T>) {
  if (!items.length) {
    return <p className="bi-chart-empty">{emptyLabel}</p>;
  }

  return (
    <div className="bi-ranking-table">
      <table>
        <thead>
          <tr>
            <th>#</th>
            {columns.map((column) => <th className={column.align === 'right' ? 'is-right' : undefined} key={column.header}>{column.header}</th>)}
          </tr>
        </thead>
        <tbody>
          {items.map((item, index) => (
            <tr key={getKey(item)}>
              <td>{index + 1}</td>
              {columns.map((column) => <td className={column.align === 'right' ? 'is-right' : undefined} key={column.header}>{column.render(item, index)}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
