import { AlertTriangle, Ban, Eye } from 'lucide-react';
import { useMemo, useState } from 'react';
import { fromDateTimeLocalValue, toDateTimeLocalValue } from './agendaControlsDate';
import type { AgendaBlockPreview, ProfessionalOption } from './agendaControls.types';

interface AgendaBlockFormProps {
  professionals: ProfessionalOption[];
  selectedProfessionalId: string;
  onProfessionalChange: (professionalId: string) => void;
  onPreview: (payload: { professionalId: string; startAt: string; endAt: string; reason: string }) => Promise<void>;
  onCreate: (confirmAffectedAppointments: boolean) => Promise<void>;
  preview: AgendaBlockPreview | null;
}

export function AgendaBlockForm({ professionals, selectedProfessionalId, onProfessionalChange, onPreview, onCreate, preview }: AgendaBlockFormProps) {
  const defaultStart = useMemo(() => {
    const value = new Date();
    value.setHours(value.getHours() + 1, 0, 0, 0);
    return toDateTimeLocalValue(value);
  }, []);
  const defaultEnd = useMemo(() => {
    const value = new Date();
    value.setHours(value.getHours() + 2, 0, 0, 0);
    return toDateTimeLocalValue(value);
  }, []);

  const [startAt, setStartAt] = useState(defaultStart);
  const [endAt, setEndAt] = useState(defaultEnd);
  const [reason, setReason] = useState('');

  async function handlePreview() {
    await onPreview({
      professionalId: selectedProfessionalId,
      startAt: fromDateTimeLocalValue(startAt),
      endAt: fromDateTimeLocalValue(endAt),
      reason
    });
  }

  return (
    <section className="agenda-control-card agenda-block-form" aria-label="Crear bloqueo de agenda">
      <div className="agenda-control-card__header">
        <span className="agenda-control-card__icon"><Ban aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="agenda-control-card__title">Bloqueo de agenda</h2>
          <p className="agenda-control-card__subtitle">Revise citas afectadas antes de bloquear.</p>
        </div>
      </div>

      <div className="agenda-block-form__grid">
        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Profesional</span>
          <select value={selectedProfessionalId} onChange={(event) => onProfessionalChange(event.target.value)}>
            {professionals.map((professional) => (
              <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>
            ))}
          </select>
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Inicio</span>
          <input type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} />
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Fin</span>
          <input type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} />
        </label>

        <label className="agenda-control-field agenda-control-field--wide">
          <span className="agenda-control-field__label">Motivo</span>
          <textarea value={reason} maxLength={300} rows={3} onChange={(event) => setReason(event.target.value)} />
        </label>
      </div>

      <div className="agenda-block-form__actions">
        <button className="agenda-control-button agenda-control-button--ghost" type="button" onClick={() => void handlePreview()} disabled={!selectedProfessionalId || !reason.trim()}>
          <Eye aria-hidden="true" size={18} />
          Preview
        </button>
        <button className="agenda-control-button agenda-control-button--primary" type="button" onClick={() => void onCreate(Boolean(preview?.requiresConfirmation))} disabled={!preview}>
          <AlertTriangle aria-hidden="true" size={18} />
          Crear bloqueo
        </button>
      </div>
    </section>
  );
}
