import { ArrowLeft, RefreshCcw, Search, Stethoscope } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  assignSpecialty,
  createProfessional,
  listProfessionals,
  listSpecialties,
  updateProfessional,
  updateProfessionalStatus
} from './professionalsApi';
import { ProfessionalForm } from './ProfessionalForm';
import { ProfessionalList } from './ProfessionalList';
import { SpecialtyAssignment } from './SpecialtyAssignment';
import type { Professional, ProfessionalPayload, Specialty } from './professionals.types';
import './professionals.css';

interface ProfessionalsPageProps {
  onBackToLogin: () => void;
}

export function ProfessionalsPage({ onBackToLogin }: ProfessionalsPageProps) {
  const [professionals, setProfessionals] = useState<Professional[]>([]);
  const [specialties, setSpecialties] = useState<Specialty[]>([]);
  const [selectedProfessional, setSelectedProfessional] = useState<Professional | undefined>();
  const [search, setSearch] = useState('');
  const [specialtyFilter, setSpecialtyFilter] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadData(searchValue = search, specialtyValue = specialtyFilter) {
    setIsLoading(true);
    setError(null);

    try {
      const [specialtiesResponse, professionalsResponse] = await Promise.all([
        listSpecialties(),
        listProfessionals({ search: searchValue, specialtyId: specialtyValue || undefined, includeInactive: true })
      ]);
      setSpecialties(specialtiesResponse);
      setProfessionals(professionalsResponse);

      if (selectedProfessional) {
        setSelectedProfessional(professionalsResponse.find((item) => item.id === selectedProfessional.id));
      }
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar profesionales');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData('', '');
  }, []);

  async function handleSubmit(payload: ProfessionalPayload) {
    setError(null);

    try {
      const saved = selectedProfessional
        ? await updateProfessional(selectedProfessional.id, payload)
        : await createProfessional(payload);
      setSelectedProfessional(saved);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar el profesional');
    }
  }

  async function handleToggleStatus(professional: Professional) {
    setError(null);

    try {
      const updated = await updateProfessionalStatus(professional.id, professional.status !== 'active');
      setSelectedProfessional((current) => (current?.id === updated.id ? updated : current));
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar el estado');
    }
  }

  async function handleAssignSpecialty(specialtyId: string, primary: boolean) {
    if (!selectedProfessional) {
      return;
    }

    setError(null);

    try {
      const updated = await assignSpecialty(selectedProfessional.id, specialtyId, primary);
      setSelectedProfessional(updated);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible asociar la especialidad');
    }
  }

  return (
    <main className="professionals-page">
      <header className="professionals-page__header">
        <button className="professionals-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="professionals-page__heading">
          <span className="professionals-page__mark"><Stethoscope aria-hidden="true" size={24} /></span>
          <div>
            <p className="professionals-page__eyebrow">I-Clinical Technology</p>
            <h1 className="professionals-page__title">Gestión de Profesionales</h1>
          </div>
        </div>
        <button className="professionals-page__refresh" type="button" onClick={() => void loadData()}>
          <RefreshCcw aria-hidden="true" size={17} />
          Actualizar
        </button>
      </header>

      <section className="professionals-toolbar" aria-label="Filtros de profesionales">
        <label className="professionals-toolbar__search">
          <Search aria-hidden="true" size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && void loadData()} placeholder="Buscar por nombre, documento o registro" />
        </label>
        <select className="professionals-toolbar__select" value={specialtyFilter} onChange={(event) => setSpecialtyFilter(event.target.value)}>
          <option value="">Todas las especialidades</option>
          {specialties.map((specialty) => (
            <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
          ))}
        </select>
        <button className="professionals-toolbar__button" type="button" onClick={() => void loadData()}>Buscar</button>
      </section>

      {error ? <div className="professionals-page__alert" role="alert">{error}</div> : null}

      <section className="professionals-page__content">
        <div className="professionals-page__list-panel">
          {isLoading ? <div className="professionals-page__loading">Cargando profesionales...</div> : (
            <ProfessionalList
              professionals={professionals}
              selectedProfessionalId={selectedProfessional?.id}
              onSelect={setSelectedProfessional}
              onToggleStatus={handleToggleStatus}
            />
          )}
        </div>

        <aside className="professionals-page__side-panel">
          <ProfessionalForm
            professional={selectedProfessional}
            specialties={specialties}
            onSubmit={handleSubmit}
            onCancel={() => setSelectedProfessional(undefined)}
          />
          <SpecialtyAssignment
            professional={selectedProfessional}
            specialties={specialties}
            onAssign={handleAssignSpecialty}
          />
        </aside>
      </section>
    </main>
  );
}
