import { Edit3, Power } from 'lucide-react';
import type { Professional } from './professionals.types';

interface ProfessionalListProps {
  professionals: Professional[];
  selectedProfessionalId?: string;
  onSelect: (professional: Professional) => void;
  onToggleStatus: (professional: Professional) => void;
}

function formatSpecialty(professional: Professional) {
  const primary = professional.specialties.find((specialty) => specialty.primary);
  return primary?.specialtyName ?? 'Sin especialidad principal';
}

export function ProfessionalList({ professionals, selectedProfessionalId, onSelect, onToggleStatus }: ProfessionalListProps) {
  return (
    <div className="professionals-list" aria-label="Listado de profesionales">
      <div className="professionals-list__header">
        <span>Profesional</span>
        <span>Especialidad</span>
        <span>Estado</span>
        <span>Acciones</span>
      </div>

      {professionals.map((professional) => {
        const fullName = `${professional.firstName} ${professional.lastName}`;
        const isSelected = professional.id === selectedProfessionalId;
        const isActive = professional.status === 'active';

        return (
          <article className={`professionals-list__row${isSelected ? ' professionals-list__row--selected' : ''}`} key={professional.id}>
            <button className="professionals-list__identity" type="button" onClick={() => onSelect(professional)}>
              <span className="professionals-list__name">{fullName}</span>
              <span className="professionals-list__meta">{professional.registryNumber || professional.documentNumber || 'Registro pendiente'}</span>
            </button>
            <span className="professionals-list__specialty">{formatSpecialty(professional)}</span>
            <span className={`professionals-list__status professionals-list__status--${professional.status}`}>
              {isActive ? 'Activo' : 'Inactivo'}
            </span>
            <div className="professionals-list__actions">
              <button className="professionals-list__icon-button" type="button" onClick={() => onSelect(professional)} aria-label={`Editar ${fullName}`}>
                <Edit3 aria-hidden="true" size={17} />
              </button>
              <button className="professionals-list__icon-button" type="button" onClick={() => onToggleStatus(professional)} aria-label={`${isActive ? 'Desactivar' : 'Activar'} ${fullName}`}>
                <Power aria-hidden="true" size={17} />
              </button>
            </div>
          </article>
        );
      })}

      {professionals.length === 0 ? (
        <div className="professionals-list__empty">No hay profesionales para los filtros actuales.</div>
      ) : null}
    </div>
  );
}
