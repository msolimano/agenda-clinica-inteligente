import { UploadCloud } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { ClinicalDocumentType, ClinicalDocumentUploadPayload, PatientOption, ProfessionalOption } from './documents.types';

const documentTypes: Array<{ value: ClinicalDocumentType; label: string }> = [
  { value: 'medical_report', label: 'Informe medico' },
  { value: 'laboratory_exam', label: 'Examen laboratorio' },
  { value: 'imaging_exam', label: 'Examen imagenologia' },
  { value: 'prescription', label: 'Receta' },
  { value: 'medical_order', label: 'Orden medica' },
  { value: 'certificate', label: 'Certificado' },
  { value: 'other', label: 'Otro' }
];

interface DocumentUploadFormProps {
  patients: PatientOption[];
  professionals: ProfessionalOption[];
  selectedPatientId: string;
  onPatientChange: (patientId: string) => void;
  onUpload: (payload: ClinicalDocumentUploadPayload) => Promise<void>;
}

export function DocumentUploadForm({ patients, professionals, selectedPatientId, onPatientChange, onUpload }: DocumentUploadFormProps) {
  const [professionalId, setProfessionalId] = useState('');
  const [documentType, setDocumentType] = useState<ClinicalDocumentType>('medical_report');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedPatientId || !file || !title.trim()) {
      return;
    }

    setIsSaving(true);
    try {
      await onUpload({
        patientId: selectedPatientId,
        professionalId: professionalId || undefined,
        documentType,
        title: title.trim(),
        description: description.trim() || undefined,
        file
      });
      setTitle('');
      setDescription('');
      setFile(null);
      event.currentTarget.reset();
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="document-upload" onSubmit={handleSubmit}>
      <div className="document-upload__header">
        <span className="document-upload__icon"><UploadCloud aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="document-upload__title">Carga documental</h2>
          <p className="document-upload__subtitle">PDF, imagen o documento Word hasta 20 MB.</p>
        </div>
      </div>

      <label className="document-field">
        <span className="document-field__label">Paciente</span>
        <select value={selectedPatientId} onChange={(event) => onPatientChange(event.target.value)} required>
          {patients.map((patient) => (
            <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}</option>
          ))}
        </select>
      </label>

      <label className="document-field">
        <span className="document-field__label">Profesional</span>
        <select value={professionalId} onChange={(event) => setProfessionalId(event.target.value)}>
          <option value="">Sin profesional</option>
          {professionals.map((professional) => (
            <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>
          ))}
        </select>
      </label>

      <label className="document-field">
        <span className="document-field__label">Tipo</span>
        <select value={documentType} onChange={(event) => setDocumentType(event.target.value as ClinicalDocumentType)}>
          {documentTypes.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
        </select>
      </label>

      <label className="document-field">
        <span className="document-field__label">Título</span>
        <input value={title} maxLength={180} onChange={(event) => setTitle(event.target.value)} required />
      </label>

      <label className="document-field">
        <span className="document-field__label">Descripción</span>
        <textarea value={description} rows={3} onChange={(event) => setDescription(event.target.value)} />
      </label>

      <label className="document-field">
        <span className="document-field__label">Archivo</span>
        <input
          type="file"
          accept="application/pdf,image/jpeg,image/png,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          required
        />
      </label>

      <button className="document-upload__submit" type="submit" disabled={isSaving || !selectedPatientId || !file || !title.trim()}>
        <UploadCloud aria-hidden="true" size={17} />
        {isSaving ? 'Cargando' : 'Cargar documento'}
      </button>
    </form>
  );
}
