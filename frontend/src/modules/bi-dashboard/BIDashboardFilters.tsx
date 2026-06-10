import { RefreshCcw } from 'lucide-react';
import type { Professional, Specialty } from '../professionals/professionals.types';

interface BIDashboardFiltersProps {
  fromDate: string;
  toDate: string;
  professionalId: string;
  specialtyId: string;
  professionals: Professional[];
  specialties: Specialty[];
  isLoading: boolean;
  onFromDateChange: (value: string) => void;
  onToDateChange: (value: string) => void;
  onProfessionalChange: (value: string) => void;
  onSpecialtyChange: (value: string) => void;
  onRefresh: () => void;
}

export function BIDashboardFilters({ fromDate, toDate, professionalId, specialtyId, professionals, specialties, isLoading, onFromDateChange, onToDateChange, onProfessionalChange, onSpecialtyChange, onRefresh }: BIDashboardFiltersProps) {
  return (
    <section className="bi-filters" aria-label="Filtros BI">
      <label className="bi-filters__field">
        Desde
        <input type="date" value={fromDate} onChange={(event) => onFromDateChange(event.target.value)} />
      </label>
      <label className="bi-filters__field">
        Hasta
        <input type="date" value={toDate} onChange={(event) => onToDateChange(event.target.value)} />
      </label>
      <label className="bi-filters__field">
        Profesional
        <select value={professionalId} onChange={(event) => onProfessionalChange(event.target.value)}>
          <option value="">Todos</option>
          {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>)}
        </select>
      </label>
      <label className="bi-filters__field">
        Especialidad
        <select value={specialtyId} onChange={(event) => onSpecialtyChange(event.target.value)}>
          <option value="">Todas</option>
          {specialties.map((specialty) => <option key={specialty.id} value={specialty.id}>{specialty.name}</option>)}
        </select>
      </label>
      <button className="bi-filters__refresh" type="button" disabled={isLoading} onClick={onRefresh}>
        <RefreshCcw aria-hidden="true" size={17} />
        Actualizar
      </button>
    </section>
  );
}
