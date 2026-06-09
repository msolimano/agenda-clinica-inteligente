import { CirclePlus, Pill } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { listClinicalRecordDiagnoses } from './clinicalDiagnosesApi';
import type { ClinicalDiagnosis } from './clinicalDiagnoses.types';
import { createClinicalPrescription, listClinicalRecordPrescriptions, updateClinicalPrescription, updateClinicalPrescriptionStatus } from './clinicalPrescriptionsApi';
import type { ClinicalPrescription, ClinicalPrescriptionPayload, ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';
import type { ClinicalRecord } from './clinicalRecords.types';
import { ClinicalPrescriptionForm } from './ClinicalPrescriptionForm';
import { ClinicalPrescriptionList } from './ClinicalPrescriptionList';

interface ClinicalPrescriptionPanelProps {
  record: ClinicalRecord;
}

export function ClinicalPrescriptionPanel({ record }: ClinicalPrescriptionPanelProps) {
  const [prescriptions, setPrescriptions] = useState<ClinicalPrescription[]>([]);
  const [diagnoses, setDiagnoses] = useState<ClinicalDiagnosis[]>([]);
  const [editingPrescription, setEditingPrescription] = useState<ClinicalPrescription | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isClosed = record.status === 'closed';
  const availableDiagnoses = useMemo(() => diagnoses.filter((diagnosis) => diagnosis.status === 'active' && diagnosis.diagnosisStatus !== 'ruled_out'), [diagnoses]);

  async function loadData() {
    setIsLoading(true);
    setError(null);
    try {
      const [prescriptionsResponse, diagnosesResponse] = await Promise.all([
        listClinicalRecordPrescriptions(record.id),
        listClinicalRecordDiagnoses(record.id)
      ]);
      setPrescriptions(prescriptionsResponse);
      setDiagnoses(diagnosesResponse);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar prescripciones clinicas');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    setEditingPrescription(null);
    setIsFormOpen(false);
    void loadData();
  }, [record.id]);

  async function handleSubmit(payload: ClinicalPrescriptionPayload, prescriptionId?: string) {
    setError(null);
    try {
      prescriptionId ? await updateClinicalPrescription(prescriptionId, payload) : await createClinicalPrescription(record.id, payload);
      setEditingPrescription(null);
      setIsFormOpen(false);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar prescripcion clinica');
      throw currentError;
    }
  }

  async function handleStatusChange(prescription: ClinicalPrescription, status: ClinicalPrescriptionStatus) {
    if (prescription.prescriptionStatus === status) {
      return;
    }

    setError(null);
    try {
      await updateClinicalPrescriptionStatus(prescription.id, status);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar estado de la prescripcion');
    }
  }

  return (
    <section className="clinical-prescription-panel" aria-label="Prescripciones clinicas">
      <div className="clinical-prescription-panel__header">
        <div className="clinical-prescription-panel__heading">
          <span className="clinical-prescription-panel__icon"><Pill aria-hidden="true" size={19} /></span>
          <div>
            <h2 className="clinical-prescription-panel__title">Prescripciones clinicas</h2>
            <p className="clinical-prescription-panel__subtitle">Medicamentos e indicaciones asociadas a esta ficha clinica.</p>
          </div>
        </div>
        <button className="clinical-prescription-panel__new" type="button" disabled={isClosed} onClick={() => { setEditingPrescription(null); setIsFormOpen(true); }}>
          <CirclePlus aria-hidden="true" size={17} />
          Nuevo
        </button>
      </div>

      {isClosed ? <p className="clinical-prescription-panel__note">La ficha esta cerrada. Las prescripciones quedan solo en lectura.</p> : null}
      {error ? <div className="clinical-prescription-panel__error" role="alert">{error}</div> : null}
      {isLoading ? <p className="clinical-prescription-panel__loading">Cargando prescripciones clinicas...</p> : null}

      {isFormOpen || editingPrescription ? (
        <ClinicalPrescriptionForm
          prescription={editingPrescription}
          diagnoses={availableDiagnoses}
          disabled={isClosed}
          onCancel={() => { setEditingPrescription(null); setIsFormOpen(false); }}
          onSubmit={handleSubmit}
        />
      ) : null}

      <ClinicalPrescriptionList prescriptions={prescriptions} disabled={isClosed} onEdit={(prescription) => { setEditingPrescription(prescription); setIsFormOpen(false); }} onStatusChange={handleStatusChange} />
    </section>
  );
}
