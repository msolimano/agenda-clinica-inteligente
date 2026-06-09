import { Pencil } from 'lucide-react';
import { ClinicalEvolutionStatusBadge } from './ClinicalEvolutionStatusBadge';
import { formatDateTime } from './clinicalRecordsDate';
import type { ClinicalEvolution, ClinicalEvolutionStatus } from './clinicalEvolutions.types';

interface ClinicalEvolutionCardProps {
  evolution: ClinicalEvolution;
  disabled: boolean;
  onEdit: (evolution: ClinicalEvolution) => void;
  onStatusChange: (evolution: ClinicalEvolution, status: ClinicalEvolutionStatus) => Promise<void>;
}

function SoapBlock({ label, value }: { label: string; value: string | null }) {
  if (!value) {
    return null;
  }
  return (
    <div className="clinical-evolution-card__soap-block">
      <h4>{label}</h4>
      <p>{value}</p>
    </div>
  );
}

export function ClinicalEvolutionCard({ evolution, disabled, onEdit, onStatusChange }: ClinicalEvolutionCardProps) {
  return (
    <article className="clinical-evolution-card">
      <div className="clinical-evolution-card__header">
        <div>
          <p className="clinical-evolution-card__date">{formatDateTime(evolution.evolutionDate)}</p>
          <ClinicalEvolutionStatusBadge status={evolution.evolutionStatus} />
        </div>
        <button className="clinical-evolution-card__edit" type="button" disabled={disabled} onClick={() => onEdit(evolution)}>
          <Pencil aria-hidden="true" size={16} />
          Editar
        </button>
      </div>

      <div className="clinical-evolution-card__soap-grid">
        <SoapBlock label="S" value={evolution.subjective} />
        <SoapBlock label="O" value={evolution.objective} />
        <SoapBlock label="A" value={evolution.assessment} />
        <SoapBlock label="P" value={evolution.plan} />
      </div>

      {evolution.notes ? <p className="clinical-evolution-card__notes"><strong>Notas:</strong> {evolution.notes}</p> : null}
      {evolution.evolutionStatus === 'corrected' ? <p className="clinical-evolution-card__trace">Nota marcada como corregida; se mantiene visible en el historial.</p> : null}
      {evolution.evolutionStatus === 'cancelled' ? <p className="clinical-evolution-card__trace">Nota cancelada; se mantiene visible como antecedente historico.</p> : null}

      <label className="clinical-evolution-card__status-control">
        Actualizar estado
        <select value={evolution.evolutionStatus} disabled={disabled} onChange={(event) => void onStatusChange(evolution, event.target.value as ClinicalEvolutionStatus)}>
          <option value="draft">Borrador</option>
          <option value="active">Activa</option>
          <option value="corrected">Corregida</option>
          <option value="cancelled">Cancelada</option>
        </select>
      </label>
    </article>
  );
}
