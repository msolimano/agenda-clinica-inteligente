import { Activity, CirclePlus } from 'lucide-react';
import { useEffect, useState } from 'react';
import { createClinicalEvolution, listClinicalRecordEvolutions, updateClinicalEvolution, updateClinicalEvolutionStatus } from './clinicalEvolutionsApi';
import type { ClinicalEvolution, ClinicalEvolutionPayload, ClinicalEvolutionStatus } from './clinicalEvolutions.types';
import type { ClinicalRecord } from './clinicalRecords.types';
import { ClinicalEvolutionForm } from './ClinicalEvolutionForm';
import { ClinicalEvolutionTimeline } from './ClinicalEvolutionTimeline';

interface ClinicalEvolutionPanelProps {
  record: ClinicalRecord;
}

export function ClinicalEvolutionPanel({ record }: ClinicalEvolutionPanelProps) {
  const [evolutions, setEvolutions] = useState<ClinicalEvolution[]>([]);
  const [editingEvolution, setEditingEvolution] = useState<ClinicalEvolution | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isClosed = record.status === 'closed';

  async function loadEvolutions() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listClinicalRecordEvolutions(record.id);
      setEvolutions(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar evoluciones clinicas');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    setEditingEvolution(null);
    setIsFormOpen(false);
    void loadEvolutions();
  }, [record.id]);

  async function handleSubmit(payload: ClinicalEvolutionPayload, evolutionId?: string) {
    setError(null);
    try {
      evolutionId ? await updateClinicalEvolution(evolutionId, payload) : await createClinicalEvolution(record.id, payload);
      setEditingEvolution(null);
      setIsFormOpen(false);
      await loadEvolutions();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar evolucion clinica');
      throw currentError;
    }
  }

  async function handleStatusChange(evolution: ClinicalEvolution, status: ClinicalEvolutionStatus) {
    if (evolution.evolutionStatus === status) {
      return;
    }

    setError(null);
    try {
      await updateClinicalEvolutionStatus(evolution.id, status);
      await loadEvolutions();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar estado de la evolucion');
    }
  }

  return (
    <section className="clinical-evolution-panel" aria-label="Evoluciones clinicas SOAP">
      <div className="clinical-evolution-panel__header">
        <div className="clinical-evolution-panel__heading">
          <span className="clinical-evolution-panel__icon"><Activity aria-hidden="true" size={19} /></span>
          <div>
            <h2 className="clinical-evolution-panel__title">Evoluciones SOAP</h2>
            <p className="clinical-evolution-panel__subtitle">Registro longitudinal de subjective, objective, assessment y plan.</p>
          </div>
        </div>
        <button className="clinical-evolution-panel__new" type="button" disabled={isClosed} onClick={() => { setEditingEvolution(null); setIsFormOpen(true); }}>
          <CirclePlus aria-hidden="true" size={17} />
          Nuevo
        </button>
      </div>

      {isClosed ? <p className="clinical-evolution-panel__note">La ficha esta cerrada. Las evoluciones quedan solo en lectura.</p> : null}
      {error ? <div className="clinical-evolution-panel__error" role="alert">{error}</div> : null}
      {isLoading ? <p className="clinical-evolution-panel__loading">Cargando evoluciones clinicas...</p> : null}

      {isFormOpen || editingEvolution ? (
        <ClinicalEvolutionForm
          evolution={editingEvolution}
          disabled={isClosed}
          onCancel={() => { setEditingEvolution(null); setIsFormOpen(false); }}
          onSubmit={handleSubmit}
        />
      ) : null}

      <ClinicalEvolutionTimeline evolutions={evolutions} disabled={isClosed} onEdit={(evolution) => { setEditingEvolution(evolution); setIsFormOpen(false); }} onStatusChange={handleStatusChange} />
    </section>
  );
}
