import { Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import type { MedicationCatalog, MedicationCatalogPayload } from './medications.types';

interface MedicationCatalogFormProps {
  medication: MedicationCatalog | null;
  onSubmit: (payload: MedicationCatalogPayload, medicationId?: string) => Promise<void>;
  onCancel: () => void;
}

export function MedicationCatalogForm({ medication, onSubmit, onCancel }: MedicationCatalogFormProps) {
  const [organizationId, setOrganizationId] = useState('');
  const [medicationCode, setMedicationCode] = useState('');
  const [medicationCodeSystem, setMedicationCodeSystem] = useState('');
  const [medicationName, setMedicationName] = useState('');
  const [activeIngredient, setActiveIngredient] = useState('');
  const [presentation, setPresentation] = useState('');
  const [strength, setStrength] = useState('');
  const [pharmaceuticalForm, setPharmaceuticalForm] = useState('');
  const [route, setRoute] = useState('');
  const [manufacturer, setManufacturer] = useState('');
  const [status, setStatus] = useState<'active' | 'inactive'>('active');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setOrganizationId(medication?.organizationId ?? '');
    setMedicationCode(medication?.medicationCode ?? '');
    setMedicationCodeSystem(medication?.medicationCodeSystem ?? '');
    setMedicationName(medication?.medicationName ?? '');
    setActiveIngredient(medication?.activeIngredient ?? '');
    setPresentation(medication?.presentation ?? '');
    setStrength(medication?.strength ?? '');
    setPharmaceuticalForm(medication?.pharmaceuticalForm ?? '');
    setRoute(medication?.route ?? '');
    setManufacturer(medication?.manufacturer ?? '');
    setStatus(medication?.status === 'inactive' ? 'inactive' : 'active');
  }, [medication]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!medicationName.trim()) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({
        organizationId,
        medicationCode,
        medicationCodeSystem,
        medicationName,
        activeIngredient,
        presentation,
        strength,
        pharmaceuticalForm,
        route,
        manufacturer,
        status
      }, medication?.id);
      if (!medication) {
        setMedicationCode('');
        setMedicationCodeSystem('');
        setMedicationName('');
        setActiveIngredient('');
        setPresentation('');
        setStrength('');
        setPharmaceuticalForm('');
        setRoute('');
        setManufacturer('');
        setStatus('active');
      }
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="medication-form" onSubmit={handleSubmit}>
      <div className="medication-form__header">
        <h2>{medication ? 'Editar medicamento' : 'Nuevo medicamento'}</h2>
        <button type="button" onClick={onCancel}>Limpiar</button>
      </div>

      <label className="medication-form__field medication-form__field--wide">
        Nombre
        <input value={medicationName} onChange={(event) => setMedicationName(event.target.value)} required />
      </label>

      <label className="medication-form__field">
        Organizacion
        <input value={organizationId} onChange={(event) => setOrganizationId(event.target.value)} placeholder="Global" />
      </label>

      <label className="medication-form__field">
        Estado
        <select value={status} onChange={(event) => setStatus(event.target.value as 'active' | 'inactive')}>
          <option value="active">Activo</option>
          <option value="inactive">Inactivo</option>
        </select>
      </label>

      <label className="medication-form__field">
        Codigo
        <input value={medicationCode} onChange={(event) => setMedicationCode(event.target.value)} />
      </label>

      <label className="medication-form__field">
        Sistema codigo
        <input value={medicationCodeSystem} onChange={(event) => setMedicationCodeSystem(event.target.value)} />
      </label>

      <label className="medication-form__field medication-form__field--wide">
        Principio activo
        <input value={activeIngredient} onChange={(event) => setActiveIngredient(event.target.value)} />
      </label>

      <label className="medication-form__field">
        Presentacion
        <input value={presentation} onChange={(event) => setPresentation(event.target.value)} />
      </label>

      <label className="medication-form__field">
        Concentracion
        <input value={strength} onChange={(event) => setStrength(event.target.value)} />
      </label>

      <label className="medication-form__field">
        Forma farmaceutica
        <input value={pharmaceuticalForm} onChange={(event) => setPharmaceuticalForm(event.target.value)} />
      </label>

      <label className="medication-form__field">
        Via
        <input value={route} onChange={(event) => setRoute(event.target.value)} />
      </label>

      <label className="medication-form__field medication-form__field--wide">
        Fabricante
        <input value={manufacturer} onChange={(event) => setManufacturer(event.target.value)} />
      </label>

      <button className="medication-form__submit" type="submit" disabled={isSaving || !medicationName.trim()}>
        <Save aria-hidden="true" size={16} />
        Guardar medicamento
      </button>
    </form>
  );
}
