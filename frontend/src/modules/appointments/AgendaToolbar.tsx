import { CalendarDays, RefreshCcw } from 'lucide-react';
import { AppointmentViewMode } from './appointments.types';

interface AgendaToolbarProps {
  date: string;
  viewMode: AppointmentViewMode;
  onDateChange: (date: string) => void;
  onViewModeChange: (viewMode: AppointmentViewMode) => void;
  onRefresh: () => void;
}

export function AgendaToolbar({ date, viewMode, onDateChange, onViewModeChange, onRefresh }: AgendaToolbarProps) {
  return (
    <section className="agenda-toolbar" aria-label="Controles de agenda">
      <label className="agenda-toolbar__date">
        <CalendarDays aria-hidden="true" size={18} />
        <input type="date" value={date} onChange={(event) => onDateChange(event.target.value)} />
      </label>
      <div className="agenda-toolbar__segment" aria-label="Vista de agenda">
        <button className={`agenda-toolbar__segment-button${viewMode === 'day' ? ' agenda-toolbar__segment-button--active' : ''}`} type="button" onClick={() => onViewModeChange('day')}>Día</button>
        <button className={`agenda-toolbar__segment-button${viewMode === 'week' ? ' agenda-toolbar__segment-button--active' : ''}`} type="button" onClick={() => onViewModeChange('week')}>Semana</button>
      </div>
      <button className="agenda-toolbar__refresh" type="button" onClick={onRefresh}>
        <RefreshCcw aria-hidden="true" size={17} />
        Actualizar
      </button>
    </section>
  );
}
