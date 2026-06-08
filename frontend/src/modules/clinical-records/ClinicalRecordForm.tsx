import { Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { fromDateTimeLocal, toDateTimeLocal } from './clinicalRecordsDate';
import type { ClinicalRecord, ClinicalRecordPayload, ClinicalRecordStatus, PatientOption, ProfessionalOption } from './clinicalRecords.types';

interface ClinicalRecordFormProps {
  patients: PatientOption[];
  professionals: ProfessionalOption[];
  selectedPatientId: string;
  record: ClinicalRecord | null;
  onSubmit: (payload: ClinicalRecordPayload, recordId?: string) => Promise<void>;
}

export function ClinicalRecordForm({ patients, professionals, selectedPatientId, record, onSubmit }: ClinicalRecordFormProps) {
  const [patientId, setPatientId] = useState(selectedPatientId);
  const [professionalId, setProfessionalId] = useState('');
  const [appointmentId, setAppointmentId] = useState('');
  const [recordDate, setRecordDate] = useState(toDateTimeLocal(null));
  const [chiefComplaint, setChiefComplaint] = useState('');
  const [anamnesis, setAnamnesis] = useState('');
  const [physicalExam, setPhysicalExam] = useState('');
  const [assessment, setAssessment] = useState('');
  const [plan, setPlan] = useState('');
  const [notes, setNotes] = useState('');
  const [status, setStatus] = useState<ClinicalRecordStatus>('draft');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (record) {
      setPatientId(record.patientId);
      setProfessionalId(record.professionalId);
      setAppointmentId(record.appointmentId ?? '');
      setRecordDate(toDateTimeLocal(record.recordDate));
      setChiefComplaint(record.chiefComplaint ?? '');
      setAnamnesis(record.anamnesis ?? '');
      setPhysicalExam(record.physicalExam ?? '');
      setAssessment(record.assessment ?? '');
      setPlan(record.plan ?? '');
      setNotes(record.notes ?? '');
      setStatus(record.status);
      return;
    }

    setPatientId(selectedPatientId);
    setProfessionalId((current) => current || professionals[0]?.id || '');
    setAppointmentId('');
    setRecordDate(toDateTimeLocal(null));
    setChiefComplaint('');
    setAnamnesis('');
    setPhysicalExam('');
    setAssessment('');
    setPlan('');
    setNotes('');
    setStatus('draft');
  }, [record, selectedPatientId, professionals]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!patientId || !professionalId || !recordDate) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({
        patientId,
        professionalId,
        appointmentId: appointmentId || null,
        recordDate: fromDateTimeLocal(recordDate),
        chiefComplaint,
        anamnesis,
        physicalExam,
        assessment,
        plan,
        notes,
        status
      }, record?.id);
    } finally {
      setIsSaving(false);
    }
  }

  const isClosed = record?.status === 'closed';

  return (
    <form className="clinical-record-form" onSubmit={handleSubmit}>
      <div className="clinical-record-form__header">
        <h2 className="clinical-record-form__title">{record ? 'Editar ficha clínica' : 'Nueva ficha clínica'}</h2>
        <p className="clinical-record-form__subtitle">Registro base de atención médica.</p>
      </div>

      <label className="clinical-record-field">
        Paciente
        <select value={patientId} onChange={(event) => setPatientId(event.target.value)} disabled={isClosed} required>
          <option value="">Seleccione paciente</option>
          {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}</option>)}
        </select>
      </label>

      <label className="clinical-record-field">
        Profesional
        <select value={professionalId} onChange={(event) => setProfessionalId(event.target.value)} disabled={isClosed} required>
          <option value="">Seleccione profesional</option>
          {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>)}
        </select>
      </label>

      <label className="clinical-record-field">
        Fecha de atención
        <input type="datetime-local" value={recordDate} onChange={(event) => setRecordDate(event.target.value)} disabled={isClosed} required />
      </label>

      <label className="clinical-record-field">
        ID cita vinculada
        <input value={appointmentId} onChange={(event) => setAppointmentId(event.target.value)} disabled={isClosed} placeholder="Opcional" />
      </label>

      <label className="clinical-record-field">
        Estado
        <select value={status} onChange={(event) => setStatus(event.target.value as ClinicalRecordStatus)} disabled={isClosed}>
          <option value="draft">Borrador</option>
          <option value="open">Abierta</option>
          <option value="closed">Cerrada</option>
        </select>
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Motivo de consulta
        <textarea value={chiefComplaint} onChange={(event) => setChiefComplaint(event.target.value)} disabled={isClosed} rows={3} />
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Anamnesis
        <textarea value={anamnesis} onChange={(event) => setAnamnesis(event.target.value)} disabled={isClosed} rows={4} />
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Examen físico
        <textarea value={physicalExam} onChange={(event) => setPhysicalExam(event.target.value)} disabled={isClosed} rows={4} />
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Evaluación
        <textarea value={assessment} onChange={(event) => setAssessment(event.target.value)} disabled={isClosed} rows={4} />
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Plan
        <textarea value={plan} onChange={(event) => setPlan(event.target.value)} disabled={isClosed} rows={4} />
      </label>

      <label className="clinical-record-field clinical-record-field--wide">
        Notas
        <textarea value={notes} onChange={(event) => setNotes(event.target.value)} disabled={isClosed} rows={3} />
      </label>

      <button className="clinical-record-form__submit" type="submit" disabled={isSaving || isClosed || !patientId || !professionalId}>
        <Save aria-hidden="true" size={17} />
        Guardar ficha
      </button>
    </form>
  );
}
