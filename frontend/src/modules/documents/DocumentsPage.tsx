import { ArrowLeft, FileArchive } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { acceptPatientAIConsent, deleteClinicalDocument, getAIProviderStatus, getPatientAIConsent, listClinicalDocuments, listPatients, listProfessionals, revokePatientAIConsent, uploadClinicalDocument } from './documentsApi';
import { AIConsentPanel } from './AIConsentPanel';
import { AIProviderNotice } from './AIProviderNotice';
import { DocumentUploadForm } from './DocumentUploadForm';
import { PatientDocumentList } from './PatientDocumentList';
import type { AIConsent, AIProviderStatus, ClinicalDocumentSummary, ClinicalDocumentUploadPayload, PatientOption, ProfessionalOption } from './documents.types';
import './documents.css';

interface DocumentsPageProps {
  onBackToLogin: () => void;
}

export function DocumentsPage({ onBackToLogin }: DocumentsPageProps) {
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [professionals, setProfessionals] = useState<ProfessionalOption[]>([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [documents, setDocuments] = useState<ClinicalDocumentSummary[]>([]);
  const [aiConsent, setAIConsent] = useState<AIConsent | null>(null);
  const [aiProviderStatus, setAIProviderStatus] = useState<AIProviderStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isConsentLoading, setIsConsentLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [consentError, setConsentError] = useState<string | null>(null);
  const [providerError, setProviderError] = useState<string | null>(null);
  const documentsRequestId = useRef(0);
  const consentRequestId = useRef(0);


  async function loadAIProviderStatus() {
    setProviderError(null);
    try {
      const response = await getAIProviderStatus();
      setAIProviderStatus(response);
    } catch (currentError) {
      setAIProviderStatus(null);
      setProviderError(currentError instanceof Error ? currentError.message : 'No fue posible cargar proveedor IA');
    }
  }

  async function loadCatalogs() {
    setIsLoading(true);
    setError(null);
    try {
      const [patientsResponse, professionalsResponse] = await Promise.all([listPatients(), listProfessionals()]);
      const activePatients = patientsResponse.filter((patient) => patient.status === 'active');
      const activeProfessionals = professionalsResponse.filter((professional) => professional.status === 'active');
      const initialPatientId = activePatients[0]?.id || '';
      setPatients(activePatients);
      setProfessionals(activeProfessionals);
      setSelectedPatientId(initialPatientId);
      await Promise.all([loadDocuments(initialPatientId), loadAIConsent(initialPatientId)]);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar pacientes y profesionales');
      setIsLoading(false);
    }
  }

  async function loadDocuments(patientId = selectedPatientId) {
    const requestId = documentsRequestId.current + 1;
    documentsRequestId.current = requestId;
    setIsLoading(true);
    setError(null);

    try {
      const response = await listClinicalDocuments(patientId ? { patientId } : {});
      if (requestId !== documentsRequestId.current) {
        return;
      }
      setDocuments(response);
      setError(null);
    } catch (currentError) {
      if (requestId !== documentsRequestId.current) {
        return;
      }
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar documentos');
    } finally {
      if (requestId === documentsRequestId.current) {
        setIsLoading(false);
      }
    }
  }


  async function loadAIConsent(patientId = selectedPatientId) {
    const requestId = consentRequestId.current + 1;
    consentRequestId.current = requestId;
    setConsentError(null);

    if (!patientId) {
      setAIConsent(null);
      setIsConsentLoading(false);
      return;
    }

    setIsConsentLoading(true);
    try {
      const response = await getPatientAIConsent(patientId);
      if (requestId !== consentRequestId.current) {
        return;
      }
      setAIConsent(response);
      setConsentError(null);
    } catch (currentError) {
      if (requestId !== consentRequestId.current) {
        return;
      }
      setAIConsent(null);
      setConsentError(currentError instanceof Error ? currentError.message : 'No fue posible cargar consentimiento IA');
    } finally {
      if (requestId === consentRequestId.current) {
        setIsConsentLoading(false);
      }
    }
  }

  useEffect(() => {
    void loadCatalogs();
    void loadAIProviderStatus();
  }, []);

  function handlePatientChange(patientId: string) {
    setSelectedPatientId(patientId);
    void loadDocuments(patientId);
    void loadAIConsent(patientId);
  }


  async function handleAcceptAIConsent() {
    if (!selectedPatientId) {
      return;
    }

    setIsConsentLoading(true);
    setConsentError(null);
    try {
      const response = await acceptPatientAIConsent(selectedPatientId);
      setAIConsent(response);
    } catch (currentError) {
      setConsentError(currentError instanceof Error ? currentError.message : 'No fue posible aceptar consentimiento IA');
    } finally {
      setIsConsentLoading(false);
    }
  }

  async function handleRevokeAIConsent() {
    if (!selectedPatientId) {
      return;
    }

    setIsConsentLoading(true);
    setConsentError(null);
    try {
      const response = await revokePatientAIConsent(selectedPatientId);
      setAIConsent(response);
    } catch (currentError) {
      setConsentError(currentError instanceof Error ? currentError.message : 'No fue posible revocar consentimiento IA');
    } finally {
      setIsConsentLoading(false);
    }
  }

  async function handleUpload(payload: ClinicalDocumentUploadPayload) {
    setError(null);
    try {
      await uploadClinicalDocument(payload);
      await loadDocuments(payload.patientId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar el documento');
    }
  }

  async function handleDelete(documentId: string) {
    setError(null);
    try {
      await deleteClinicalDocument(documentId);
      await loadDocuments();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible eliminar el documento');
    }
  }

  return (
    <main className="documents-page">
      <header className="documents-page__header">
        <button className="documents-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="documents-page__heading">
          <span className="documents-page__mark"><FileArchive aria-hidden="true" size={24} /></span>
          <div>
            <p className="documents-page__eyebrow">I-Clinical Technology</p>
            <h1 className="documents-page__title">Gestión Documental</h1>
          </div>
        </div>
        <div className="documents-page__counter">
          <strong>{documents.length}</strong>
          <span>documentos activos</span>
        </div>
      </header>

      {error ? <div className="documents-page__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="documents-page__loading">Cargando documentos...</div> : null}

      <section className="documents-page__content">
        <aside className="documents-page__side">
          <DocumentUploadForm
            patients={patients}
            professionals={professionals}
            selectedPatientId={selectedPatientId}
            onPatientChange={handlePatientChange}
            onUpload={handleUpload}
          />
          <AIConsentPanel
            consent={aiConsent}
            hasPatient={Boolean(selectedPatientId)}
            isLoading={isConsentLoading}
            error={consentError}
            onAccept={handleAcceptAIConsent}
            onRevoke={handleRevokeAIConsent}
          />
          <AIProviderNotice status={aiProviderStatus} error={providerError} />
        </aside>
        <div className="documents-page__main">
          <PatientDocumentList documents={documents} aiConsent={aiConsent} onDelete={handleDelete} onRefresh={() => loadDocuments()} />
        </div>
      </section>
    </main>
  );
}
