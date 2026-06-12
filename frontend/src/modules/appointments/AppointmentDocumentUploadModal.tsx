import { CheckCircle2, FileUp, FolderOpen, Stethoscope, UserRound, X } from 'lucide-react';
import { FormEvent, useMemo, useState } from 'react';
import { uploadClinicalDocument } from '../documents/documentsApi';
import type { ClinicalDocumentType } from '../documents/documents.types';
import type { Appointment } from './appointments.types';

const documentTypes: Array<{ value: ClinicalDocumentType; label: string }> = [
  { value: 'medical_report', label: 'Informe medico' },
  { value: 'laboratory_exam', label: 'Examen laboratorio' },
  { value: 'imaging_exam', label: 'Examen imagenologia' },
  { value: 'prescription', label: 'Receta' },
  { value: 'medical_order', label: 'Orden medica' },
  { value: 'certificate', label: 'Certificado' },
  { value: 'other', label: 'Otro' }
];

interface AppointmentDocumentUploadModalProps {
  appointment: Appointment;
  onClose: () => void;
}

export function AppointmentDocumentUploadModal({ appointment, onClose }: AppointmentDocumentUploadModalProps) {
  const [isUploadVisible, setIsUploadVisible] = useState(false);
  const [documentType, setDocumentType] = useState<ClinicalDocumentType>('medical_report');
  const [title, setTitle] = useState('Documento preconsulta');
  const [description, setDescription] = useState(`Documento preconsulta asociado a cita ${appointment.id}`);
  const [file, setFile] = useState<File | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isUploaded, setIsUploaded] = useState(false);

  const appointmentDate = useMemo(() => {
    return new Intl.DateTimeFormat('es-CL', {
      dateStyle: 'medium',
      timeStyle: 'short'
    }).format(new Date(appointment.startAt));
  }, [appointment.startAt]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!appointment.patientId || !file || !title.trim()) {
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      await uploadClinicalDocument({
        patientId: appointment.patientId,
        professionalId: appointment.professionalId || undefined,
        documentType,
        title: title.trim(),
        description: description.trim() || `Documento preconsulta asociado a cita ${appointment.id}`,
        file
      });
      setIsUploaded(true);
      setFile(null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar el documento');
    } finally {
      setIsSaving(false);
    }
  }

  function goToDocuments() {
    window.location.hash = '#/documents';
  }

  function goToPatientPortal() {
    window.location.hash = '#/patient-portal';
  }

  return (
    <div className="appointment-document-modal" role="dialog" aria-modal="true" aria-labelledby="appointment-document-modal-title">
      <div className="appointment-document-modal__backdrop" onClick={onClose} />
      <section className="appointment-document-modal__panel">
        <button className="appointment-document-modal__close" type="button" onClick={onClose} aria-label="Cerrar carga documental">
          <X aria-hidden="true" size={18} />
        </button>

        <div className="appointment-document-modal__header">
          <span className="appointment-document-modal__icon"><FileUp aria-hidden="true" size={22} /></span>
          <div>
            <p className="appointment-document-modal__eyebrow">Documentos preconsulta</p>
            <h2 className="appointment-document-modal__title" id="appointment-document-modal-title">Cita creada correctamente</h2>
            <p className="appointment-document-modal__subtitle">Puede cargar documentos clinicos para esta atencion.</p>
          </div>
        </div>

        <div className="appointment-document-modal__summary" aria-label="Resumen de la cita creada">
          <span><UserRound aria-hidden="true" size={16} /> {appointment.patientName ?? 'Paciente seleccionado'}</span>
          <span><Stethoscope aria-hidden="true" size={16} /> {appointment.professionalName}</span>
          <span>{appointmentDate}</span>
        </div>

        {!isUploadVisible && !isUploaded ? (
          <div className="appointment-document-modal__actions">
            <button className="appointment-document-modal__primary" type="button" onClick={() => setIsUploadVisible(true)}>
              <FileUp aria-hidden="true" size={17} />
              Cargar documentos
            </button>
            <button className="appointment-document-modal__secondary" type="button" onClick={onClose}>Omitir por ahora</button>
          </div>
        ) : null}

        {isUploadVisible && !isUploaded ? (
          <form className="appointment-document-form" onSubmit={handleSubmit}>
            {error ? <div className="appointment-document-form__alert" role="alert">{error}</div> : null}

            <label className="appointment-document-form__field">
              <span>Tipo de documento</span>
              <select value={documentType} onChange={(event) => setDocumentType(event.target.value as ClinicalDocumentType)}>
                {documentTypes.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
              </select>
            </label>

            <label className="appointment-document-form__field">
              <span>Titulo</span>
              <input value={title} maxLength={180} onChange={(event) => setTitle(event.target.value)} required />
            </label>

            <label className="appointment-document-form__field">
              <span>Descripcion</span>
              <textarea value={description} rows={3} onChange={(event) => setDescription(event.target.value)} />
            </label>

            <label className="appointment-document-form__field">
              <span>Archivo</span>
              <input
                type="file"
                accept="application/pdf,image/jpeg,image/png,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
                required
              />
            </label>

            <div className="appointment-document-modal__actions appointment-document-modal__actions--form">
              <button className="appointment-document-modal__primary" type="submit" disabled={isSaving || !file || !title.trim()}>
                <FileUp aria-hidden="true" size={17} />
                {isSaving ? 'Cargando' : 'Cargar documento'}
              </button>
              <button className="appointment-document-modal__secondary" type="button" onClick={onClose}>Omitir por ahora</button>
            </div>
          </form>
        ) : null}

        {isUploaded ? (
          <div className="appointment-document-modal__success" role="status">
            <CheckCircle2 aria-hidden="true" size={24} />
            <div>
              <h3>Documento cargado correctamente.</h3>
              <p>El documento quedara disponible en Documentos Clinicos y en el Portal Paciente.</p>
            </div>
            <div className="appointment-document-modal__actions appointment-document-modal__actions--success">
              <button className="appointment-document-modal__secondary" type="button" onClick={onClose}>Cerrar</button>
              <button className="appointment-document-modal__secondary" type="button" onClick={goToPatientPortal}>Ir a Portal Paciente</button>
              <button className="appointment-document-modal__primary" type="button" onClick={goToDocuments}>
                <FolderOpen aria-hidden="true" size={17} />
                Ir a Documentos
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </div>
  );
}
