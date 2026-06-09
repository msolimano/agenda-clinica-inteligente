import { Save, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { MedicationSearchInput } from '../medications/MedicationSearchInput';
import type { MedicationCatalog } from '../medications/medications.types';
import type { ClinicalDiagnosis } from './clinicalDiagnoses.types';
import type { ClinicalPrescription, ClinicalPrescriptionPayload, ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';

interface ClinicalPrescriptionFormProps {
  prescription: ClinicalPrescription | null;
  diagnoses: ClinicalDiagnosis[];
  organizationId?: string | null;
  disabled: boolean;
  onCancel: () => void;
  onSubmit: (payload: ClinicalPrescriptionPayload, prescriptionId?: string) => Promise<void>;
}

export function ClinicalPrescriptionForm({ prescription, diagnoses, organizationId, disabled, onCancel, onSubmit }: ClinicalPrescriptionFormProps) {
  const [diagnosisId, setDiagnosisId] = useState('');
  const [medicationCatalogId, setMedicationCatalogId] = useState('');
  const [selectedMedicationLabel, setSelectedMedicationLabel] = useState<string | null>(null);
  const [medicationName, setMedicationName] = useState('');
  const [dosage, setDosage] = useState('');
  const [frequency, setFrequency] = useState('');
  const [duration, setDuration] = useState('');
  const [route, setRoute] = useState('');
  const [patientInstructions, setPatientInstructions] = useState('');
  const [clinicalNotes, setClinicalNotes] = useState('');
  const [prescriptionStatus, setPrescriptionStatus] = useState<ClinicalPrescriptionStatus>('draft');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setDiagnosisId(prescription?.diagnosisId ?? '');
    setMedicationCatalogId(prescription?.medicationCatalogId ?? '');
    setSelectedMedicationLabel(prescription?.medicationCatalogId ? prescription.medicationCodeDisplay || prescription.medicationName : null);
    setMedicationName(prescription?.medicationName ?? '');
    setDosage(prescription?.dosage ?? '');
    setFrequency(prescription?.frequency ?? '');
    setDuration(prescription?.duration ?? '');
    setRoute(prescription?.route ?? '');
    setPatientInstructions(prescription?.patientInstructions ?? '');
    setClinicalNotes(prescription?.clinicalNotes ?? '');
    setPrescriptionStatus(prescription?.prescriptionStatus ?? 'draft');
  }, [prescription]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!medicationName.trim() || !dosage.trim() || !frequency.trim() || disabled) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({
        diagnosisId: diagnosisId || null,
        medicationCatalogId: medicationCatalogId || null,
        medicationName,
        dosage,
        frequency,
        duration,
        route,
        patientInstructions,
        clinicalNotes,
        prescriptionStatus
      }, prescription?.id);
      if (!prescription) {
        setDiagnosisId('');
        setMedicationCatalogId('');
        setSelectedMedicationLabel(null);
        setMedicationName('');
        setDosage('');
        setFrequency('');
        setDuration('');
        setRoute('');
        setPatientInstructions('');
        setClinicalNotes('');
        setPrescriptionStatus('draft');
      }
    } finally {
      setIsSaving(false);
    }
  }


  function handleMedicationSelect(medication: MedicationCatalog) {
    setMedicationCatalogId(medication.id);
    setSelectedMedicationLabel(medication.medicationName);
    setMedicationName(medication.medicationName);
    if (medication.route) {
      setRoute(medication.route);
    }
  }

  function handleMedicationClear() {
    setMedicationCatalogId('');
    setSelectedMedicationLabel(null);
  }

  return (
    <form className="clinical-prescription-form" onSubmit={handleSubmit}>
      <div className="clinical-prescription-form__header">
        <h3>{prescription ? 'Editar prescripcion' : 'Nueva prescripcion'}</h3>
        <button className="clinical-prescription-form__close" type="button" onClick={onCancel}>
          <X aria-hidden="true" size={16} />
        </button>
      </div>

      <label className="clinical-prescription-field">
        Diagnostico asociado
        <select value={diagnosisId} onChange={(event) => setDiagnosisId(event.target.value)} disabled={disabled}>
          <option value="">Sin diagnostico asociado</option>
          {diagnoses.map((diagnosis) => <option key={diagnosis.id} value={diagnosis.id}>{diagnosis.diagnosisText}</option>)}
        </select>
      </label>

      <MedicationSearchInput
        organizationId={organizationId}
        disabled={disabled}
        selectedLabel={selectedMedicationLabel}
        onSelect={handleMedicationSelect}
        onClear={handleMedicationClear}
      />

      <label className="clinical-prescription-field clinical-prescription-field--wide">
        Medicamento
        <textarea value={medicationName} onChange={(event) => { setMedicationName(event.target.value); if (medicationCatalogId) { setMedicationCatalogId(''); setSelectedMedicationLabel(null); } }} disabled={disabled} rows={2} required />
      </label>

      <label className="clinical-prescription-field">
        Dosis
        <input value={dosage} onChange={(event) => setDosage(event.target.value)} disabled={disabled} required />
      </label>

      <label className="clinical-prescription-field">
        Frecuencia
        <input value={frequency} onChange={(event) => setFrequency(event.target.value)} disabled={disabled} required />
      </label>

      <label className="clinical-prescription-field">
        Duracion
        <input value={duration} onChange={(event) => setDuration(event.target.value)} disabled={disabled} />
      </label>

      <label className="clinical-prescription-field">
        Via de administracion
        <input value={route} onChange={(event) => setRoute(event.target.value)} disabled={disabled} />
      </label>

      <label className="clinical-prescription-field">
        Estado
        <select value={prescriptionStatus} onChange={(event) => setPrescriptionStatus(event.target.value as ClinicalPrescriptionStatus)} disabled={disabled}>
          <option value="draft">Borrador</option>
          <option value="active">Activa</option>
          <option value="suspended">Suspendida</option>
          <option value="completed">Completada</option>
          <option value="cancelled">Cancelada</option>
        </select>
      </label>

      <label className="clinical-prescription-field clinical-prescription-field--wide">
        Indicaciones al paciente <span>Recomendado</span>
        <textarea value={patientInstructions} onChange={(event) => setPatientInstructions(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <label className="clinical-prescription-field clinical-prescription-field--wide">
        Observaciones clinicas
        <textarea value={clinicalNotes} onChange={(event) => setClinicalNotes(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <button className="clinical-prescription-form__submit" type="submit" disabled={disabled || isSaving || !medicationName.trim() || !dosage.trim() || !frequency.trim()}>
        <Save aria-hidden="true" size={16} />
        Guardar prescripcion
      </button>
    </form>
  );
}
