import { Filter, RefreshCcw } from 'lucide-react';
import type { ProfessionalOption, SpecialtyOption } from './waitingList.types';

interface WaitingListToolbarProps {
  specialties: SpecialtyOption[];
  professionals: ProfessionalOption[];
  specialtyFilter: string;
  professionalFilter: string;
  statusFilter: string;
  onSpecialtyFilterChange: (value: string) => void;
  onProfessionalFilterChange: (value: string) => void;
  onStatusFilterChange: (value: string) => void;
  onRefresh: () => void;
}

export function WaitingListToolbar({ specialties, professionals, specialtyFilter, professionalFilter, statusFilter, onSpecialtyFilterChange, onProfessionalFilterChange, onStatusFilterChange, onRefresh }: WaitingListToolbarProps) {
  return (
    <section className="waiting-toolbar" aria-label="Filtros de lista de espera">
      <span className="waiting-toolbar__icon"><Filter aria-hidden="true" size={18} /></span>
      <select value={specialtyFilter} onChange={(event) => onSpecialtyFilterChange(event.target.value)}>
        <option value="">Todas las especialidades</option>
        {specialties.map((specialty) => <option key={specialty.id} value={specialty.id}>{specialty.name}</option>)}
      </select>
      <select value={professionalFilter} onChange={(event) => onProfessionalFilterChange(event.target.value)}>
        <option value="">Todos los profesionales</option>
        {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>)}
      </select>
      <select value={statusFilter} onChange={(event) => onStatusFilterChange(event.target.value)}>
        <option value="">Todos los estados</option>
        <option value="waiting">En espera</option>
        <option value="contacted">Contactado</option>
        <option value="scheduled">Agendado</option>
        <option value="cancelled">Cancelado</option>
      </select>
      <button className="waiting-toolbar__refresh" type="button" onClick={onRefresh}>
        <RefreshCcw aria-hidden="true" size={17} />
        Actualizar
      </button>
    </section>
  );
}
