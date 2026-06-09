import { ArrowLeft, ClipboardList, RefreshCcw, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import { createDiagnosisCatalogItem, listDiagnosisCatalog, updateDiagnosisCatalogItem, updateDiagnosisCatalogStatus } from './diagnosisCatalogApi';
import { DiagnosisCatalogForm } from './DiagnosisCatalogForm';
import { DiagnosisCatalogTable } from './DiagnosisCatalogTable';
import type { DiagnosisCatalog, DiagnosisCatalogPayload } from './diagnosisCatalog.types';
import './diagnosis-catalog.css';

interface DiagnosisCatalogPageProps {
  onBackToLogin: () => void;
}

export function DiagnosisCatalogPage({ onBackToLogin }: DiagnosisCatalogPageProps) {
  const [diagnoses, setDiagnoses] = useState<DiagnosisCatalog[]>([]);
  const [selectedDiagnosis, setSelectedDiagnosis] = useState<DiagnosisCatalog | null>(null);
  const [search, setSearch] = useState('');
  const [includeInactive, setIncludeInactive] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadData(searchValue = search) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listDiagnosisCatalog({ search: searchValue, includeInactive });
      setDiagnoses(response);
      if (selectedDiagnosis) {
        setSelectedDiagnosis(response.find((item) => item.id === selectedDiagnosis.id) ?? null);
      }
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar diagnosticos');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData('');
  }, []);

  async function handleSubmit(payload: DiagnosisCatalogPayload, diagnosisId?: string) {
    setError(null);
    try {
      const saved = diagnosisId ? await updateDiagnosisCatalogItem(diagnosisId, payload) : await createDiagnosisCatalogItem(payload);
      setSelectedDiagnosis(saved);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar diagnostico');
    }
  }

  async function handleToggleStatus(diagnosis: DiagnosisCatalog) {
    setError(null);
    try {
      const updated = await updateDiagnosisCatalogStatus(diagnosis.id, diagnosis.status === 'active' ? 'inactive' : 'active');
      setSelectedDiagnosis((current) => current?.id === updated.id ? updated : current);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar estado');
    }
  }

  return (
    <main className="diagnosis-catalog-page">
      <header className="diagnosis-catalog-page__header">
        <button className="diagnosis-catalog-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="diagnosis-catalog-page__heading">
          <span className="diagnosis-catalog-page__mark"><ClipboardList aria-hidden="true" size={24} /></span>
          <div>
            <p className="diagnosis-catalog-page__eyebrow">I-Clinical Technology</p>
            <h1 className="diagnosis-catalog-page__title">Catalogo de Diagnosticos</h1>
          </div>
        </div>
        <button className="diagnosis-catalog-page__refresh" type="button" onClick={() => void loadData()}>
          <RefreshCcw aria-hidden="true" size={17} />
          Actualizar
        </button>
      </header>

      <section className="diagnosis-catalog-toolbar" aria-label="Filtros de diagnosticos">
        <label className="diagnosis-catalog-toolbar__search">
          <Search aria-hidden="true" size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && void loadData()} placeholder="Buscar por diagnostico, codigo o categoria" />
        </label>
        <label className="diagnosis-catalog-toolbar__check">
          <input type="checkbox" checked={includeInactive} onChange={(event) => setIncludeInactive(event.target.checked)} />
          Inactivos
        </label>
        <button className="diagnosis-catalog-toolbar__button" type="button" onClick={() => void loadData()}>Buscar</button>
      </section>

      {error ? <div className="diagnosis-catalog-page__alert" role="alert">{error}</div> : null}

      <section className="diagnosis-catalog-page__content">
        <div className="diagnosis-catalog-page__list-panel">
          {isLoading ? <div className="diagnosis-catalog-page__loading">Cargando diagnosticos...</div> : (
            <DiagnosisCatalogTable diagnoses={diagnoses} selectedDiagnosisId={selectedDiagnosis?.id} onSelect={setSelectedDiagnosis} onToggleStatus={handleToggleStatus} />
          )}
        </div>
        <aside className="diagnosis-catalog-page__side-panel">
          <DiagnosisCatalogForm diagnosis={selectedDiagnosis} onSubmit={handleSubmit} onCancel={() => setSelectedDiagnosis(null)} />
        </aside>
      </section>
    </main>
  );
}
