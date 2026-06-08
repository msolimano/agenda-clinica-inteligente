import { Clock3, Info } from 'lucide-react';
import { formatDateTime } from './agendaControlsDate';
import type { AgendaBlockPreview } from './agendaControls.types';

interface AgendaBlockPreviewProps {
  preview: AgendaBlockPreview | null;
}

export function AgendaBlockPreview({ preview }: AgendaBlockPreviewProps) {
  if (!preview) {
    return (
      <section className="agenda-control-card agenda-block-preview" aria-label="Preview de bloqueo">
        <div className="agenda-control-card__header">
          <span className="agenda-control-card__icon"><Info aria-hidden="true" size={20} /></span>
          <div>
            <h2 className="agenda-control-card__title">Preview</h2>
            <p className="agenda-control-card__subtitle">Aun no hay una simulacion calculada.</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="agenda-control-card agenda-block-preview" aria-label="Resultado del preview de bloqueo">
      <div className="agenda-control-card__header">
        <span className="agenda-control-card__icon"><Info aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="agenda-control-card__title">Preview</h2>
          <p className="agenda-control-card__subtitle">{preview.requiresConfirmation ? 'Requiere confirmacion por citas afectadas.' : 'No se detectaron citas afectadas.'}</p>
        </div>
      </div>

      <div className="agenda-block-preview__summary">
        <span><strong>{preview.affectedAppointments.length}</strong> citas afectadas</span>
        <span><strong>{preview.rescheduleSuggestions.reduce((total, item) => total + item.suggestedSlots.length, 0)}</strong> alternativas sugeridas</span>
      </div>

      <div className="agenda-block-preview__list">
        {preview.affectedAppointments.map((appointment) => {
          const suggestion = preview.rescheduleSuggestions.find((item) => item.appointmentId === appointment.appointmentId);

          return (
            <article className="agenda-affected" key={appointment.appointmentId}>
              <div>
                <h3 className="agenda-affected__title">{appointment.patientName ?? 'Paciente sin nombre'}</h3>
                <p className="agenda-affected__meta">{formatDateTime(appointment.startAt)} - {formatDateTime(appointment.endAt)}</p>
              </div>
              <div className="agenda-affected__suggestions">
                {suggestion?.suggestedSlots.length ? suggestion.suggestedSlots.map((slot) => (
                  <span className="agenda-affected__slot" key={`${appointment.appointmentId}-${slot.startAt}`}>
                    <Clock3 aria-hidden="true" size={14} />
                    {formatDateTime(slot.startAt)}
                  </span>
                )) : <span className="agenda-affected__empty">Sin alternativas inmediatas</span>}
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
