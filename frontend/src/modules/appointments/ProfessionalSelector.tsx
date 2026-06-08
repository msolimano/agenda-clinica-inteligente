import { Stethoscope } from 'lucide-react';
import type { ProfessionalOption } from './appointments.types';

interface ProfessionalSelectorProps {
  professionals: ProfessionalOption[];
  selectedProfessionalId: string;
  onChange: (professionalId: string) => void;
}

export function ProfessionalSelector({ professionals, selectedProfessionalId, onChange }: ProfessionalSelectorProps) {
  return (
    <label className="agenda-professional-selector">
      <Stethoscope aria-hidden="true" size={18} />
      <select value={selectedProfessionalId} onChange={(event) => onChange(event.target.value)}>
        {professionals.map((professional) => (
          <option key={professional.id} value={professional.id}>
            {professional.firstName} {professional.lastName}
          </option>
        ))}
      </select>
    </label>
  );
}
