import { ArrowLeft, Pill, RefreshCcw, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import { createMedication, listMedications, updateMedication, updateMedicationStatus } from './medicationsApi';
import { MedicationCatalogForm } from './MedicationCatalogForm';
import { MedicationCatalogTable } from './MedicationCatalogTable';
import type { MedicationCatalog, MedicationCatalogPayload } from './medications.types';
import './medications.css';

interface MedicationCatalogPageProps {
  onBackToLogin: () => void;
}

export function MedicationCatalogPage({ onBackToLogin }: MedicationCatalogPageProps) {
  const [medications, setMedications] = useState<MedicationCatalog[]>([]);
  const [selectedMedication, setSelectedMedication] = useState<MedicationCatalog | null>(null);
  const [search, setSearch] = useState('');
  const [includeInactive, setIncludeInactive] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadData(searchValue = search) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listMedications({ search: searchValue, includeInactive });
      setMedications(response);
      if (selectedMedication) {
        setSelectedMedication(response.find((item) => item.id === selectedMedication.id) ?? null);
      }
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar medicamentos');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData('');
  }, []);

  async function handleSubmit(payload: MedicationCatalogPayload, medicationId?: string) {
    setError(null);
    try {
      const saved = medicationId ? await updateMedication(medicationId, payload) : await createMedication(payload);
      setSelectedMedication(saved);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar medicamento');
    }
  }

  async function handleToggleStatus(medication: MedicationCatalog) {
    setError(null);
    try {
      const updated = await updateMedicationStatus(medication.id, medication.status === 'active' ? 'inactive' : 'active');
      setSelectedMedication((current) => current?.id === updated.id ? updated : current);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar estado');
    }
  }

  return (
    <main className="medications-page">
      <header className="medications-page__header">
        <button className="medications-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="medications-page__heading">
          <span className="medications-page__mark"><Pill aria-hidden="true" size={24} /></span>
          <div>
            <p className="medications-page__eyebrow">I-Clinical Technology</p>
            <h1 className="medications-page__title">Catalogo de Medicamentos</h1>
          </div>
        </div>
        <button className="medications-page__refresh" type="button" onClick={() => void loadData()}>
          <RefreshCcw aria-hidden="true" size={17} />
          Actualizar
        </button>
      </header>

      <section className="medications-toolbar" aria-label="Filtros de medicamentos">
        <label className="medications-toolbar__search">
          <Search aria-hidden="true" size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && void loadData()} placeholder="Buscar por nombre, principio activo o codigo" />
        </label>
        <label className="medications-toolbar__check">
          <input type="checkbox" checked={includeInactive} onChange={(event) => setIncludeInactive(event.target.checked)} />
          Inactivos
        </label>
        <button className="medications-toolbar__button" type="button" onClick={() => void loadData()}>Buscar</button>
      </section>

      {error ? <div className="medications-page__alert" role="alert">{error}</div> : null}

      <section className="medications-page__content">
        <div className="medications-page__list-panel">
          {isLoading ? <div className="medications-page__loading">Cargando medicamentos...</div> : (
            <MedicationCatalogTable medications={medications} selectedMedicationId={selectedMedication?.id} onSelect={setSelectedMedication} onToggleStatus={handleToggleStatus} />
          )}
        </div>
        <aside className="medications-page__side-panel">
          <MedicationCatalogForm medication={selectedMedication} onSubmit={handleSubmit} onCancel={() => setSelectedMedication(null)} />
        </aside>
      </section>
    </main>
  );
}
