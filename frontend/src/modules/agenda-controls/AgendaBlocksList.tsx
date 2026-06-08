import { CalendarOff, XCircle } from 'lucide-react';
import { formatDateTime } from './agendaControlsDate';
import type { AgendaBlock } from './agendaControls.types';

interface AgendaBlocksListProps {
  blocks: AgendaBlock[];
  onCancel: (blockId: string) => Promise<void>;
}

export function AgendaBlocksList({ blocks, onCancel }: AgendaBlocksListProps) {
  return (
    <section className="agenda-control-card agenda-blocks-list" aria-label="Bloqueos activos">
      <div className="agenda-control-card__header">
        <span className="agenda-control-card__icon"><CalendarOff aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="agenda-control-card__title">Bloqueos</h2>
          <p className="agenda-control-card__subtitle">Rangos activos y citas afectadas.</p>
        </div>
      </div>

      <div className="agenda-blocks-list__items">
        {blocks.length ? blocks.map((block) => (
          <article className="agenda-block-item" key={block.id}>
            <div>
              <h3 className="agenda-block-item__title">{formatDateTime(block.startAt)} - {formatDateTime(block.endAt)}</h3>
              <p className="agenda-block-item__reason">{block.reason}</p>
              <p className="agenda-block-item__meta">{block.affectedAppointments.length} citas afectadas</p>
            </div>
            <button className="agenda-control-icon-button" type="button" aria-label="Cancelar bloqueo" onClick={() => void onCancel(block.id)}>
              <XCircle aria-hidden="true" size={18} />
            </button>
          </article>
        )) : <p className="agenda-control-empty">No hay bloqueos activos en el rango.</p>}
      </div>
    </section>
  );
}
