import { ClinicalEvolutionCard } from './ClinicalEvolutionCard';
import type { ClinicalEvolution, ClinicalEvolutionStatus } from './clinicalEvolutions.types';

interface ClinicalEvolutionTimelineProps {
  evolutions: ClinicalEvolution[];
  disabled: boolean;
  onEdit: (evolution: ClinicalEvolution) => void;
  onStatusChange: (evolution: ClinicalEvolution, status: ClinicalEvolutionStatus) => Promise<void>;
}

export function ClinicalEvolutionTimeline({ evolutions, disabled, onEdit, onStatusChange }: ClinicalEvolutionTimelineProps) {
  if (!evolutions.length) {
    return <p className="clinical-evolution-timeline__empty">No hay evoluciones clinicas registradas.</p>;
  }

  return (
    <div className="clinical-evolution-timeline">
      {evolutions.map((evolution) => (
        <ClinicalEvolutionCard
          key={evolution.id}
          evolution={evolution}
          disabled={disabled}
          onEdit={onEdit}
          onStatusChange={onStatusChange}
        />
      ))}
    </div>
  );
}
