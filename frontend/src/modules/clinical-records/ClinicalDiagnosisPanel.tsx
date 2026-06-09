import { CirclePlus, Stethoscope } from 'lucide-react';
import { useEffect, useState } from 'react';
import { ClinicalDiagnosisForm } from './ClinicalDiagnosisForm';
import { ClinicalDiagnosisList } from './ClinicalDiagnosisList';
import { createClinicalDiagnosis, listClinicalRecordDiagnoses, updateClinicalDiagnosis, updateClinicalDiagnosisStatus } from './clinicalDiagnosesApi';
import type { ClinicalDiagnosis, ClinicalDiagnosisPayload, ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';
import type { ClinicalRecord } from './clinicalRecords.types';

interface ClinicalDiagnosisPanelProps {
  record: ClinicalRecord;
}

export function ClinicalDiagnosisPanel({ record }: ClinicalDiagnosisPanelProps) {
  const [diagnoses, setDiagnoses] = useState<ClinicalDiagnosis[]>([]);
  const [editingDiagnosis, setEditingDiagnosis] = useState<ClinicalDiagnosis | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isClosed = record.status === 'closed';

  async function loadDiagnoses() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listClinicalRecordDiagnoses(record.id);
      setDiagnoses(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar diagnosticos clinicos');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    setEditingDiagnosis(null);
    setIsFormOpen(false);
    void loadDiagnoses();
  }, [record.id]);

  async function handleSubmit(payload: ClinicalDiagnosisPayload, diagnosisId?: string) {
    setError(null);
    try {
      diagnosisId ? await updateClinicalDiagnosis(diagnosisId, payload) : await createClinicalDiagnosis(record.id, payload);
      setEditingDiagnosis(null);
      setIsFormOpen(false);
      await loadDiagnoses();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar diagnostico clinico');
      throw currentError;
    }
  }

  async function handleStatusChange(diagnosis: ClinicalDiagnosis, status: ClinicalDiagnosisStatus) {
    if (diagnosis.diagnosisStatus === status) {
      return;
    }

    setError(null);
    try {
      await updateClinicalDiagnosisStatus(diagnosis.id, status);
      await loadDiagnoses();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar estado del diagnostico');
    }
  }

  return (
    <section className="clinical-diagnosis-panel" aria-label="Diagnosticos clinicos">
      <div className="clinical-diagnosis-panel__header">
        <div className="clinical-diagnosis-panel__heading">
          <span className="clinical-diagnosis-panel__icon"><Stethoscope aria-hidden="true" size={19} /></span>
          <div>
            <h2 className="clinical-diagnosis-panel__title">Diagnosticos clinicos</h2>
            <p className="clinical-diagnosis-panel__subtitle">Condiciones asociadas a esta ficha clinica.</p>
          </div>
        </div>
        <button className="clinical-diagnosis-panel__new" type="button" disabled={isClosed} onClick={() => { setEditingDiagnosis(null); setIsFormOpen(true); }}>
          <CirclePlus aria-hidden="true" size={17} />
          Nuevo
        </button>
      </div>

      {isClosed ? <p className="clinical-diagnosis-panel__note">La ficha esta cerrada. Los diagnosticos quedan solo en lectura.</p> : null}
      {error ? <div className="clinical-diagnosis-panel__error" role="alert">{error}</div> : null}
      {isLoading ? <p className="clinical-diagnosis-panel__loading">Cargando diagnosticos clinicos...</p> : null}

      {isFormOpen || editingDiagnosis ? (
        <ClinicalDiagnosisForm
          diagnosis={editingDiagnosis}
          organizationId={record.organizationId}
          disabled={isClosed}
          onCancel={() => { setEditingDiagnosis(null); setIsFormOpen(false); }}
          onSubmit={handleSubmit}
        />
      ) : null}

      <ClinicalDiagnosisList diagnoses={diagnoses} disabled={isClosed} onEdit={(diagnosis) => { setEditingDiagnosis(diagnosis); setIsFormOpen(false); }} onStatusChange={handleStatusChange} />
    </section>
  );
}
