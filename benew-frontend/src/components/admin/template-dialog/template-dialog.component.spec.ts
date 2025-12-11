import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { TemplateDialogComponent } from './template-dialog.component';
import { TemplateService } from '../../../services/template.service';
import { TemplateDto } from '../../../models/template.dto';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TemplateDialogComponent', () => {
  const mockTemplate: TemplateDto = {
    id: 'template-1',
    positionName: 'Developer',
    description: 'Developer onboarding',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  };

  describe('Create Mode', () => {
    let component: TemplateDialogComponent;
    let fixture: ComponentFixture<TemplateDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplate',
        'updateTemplate',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TemplateDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;
      
      fixture = TestBed.createComponent(TemplateDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in create mode', () => {
      expect(component).toBeTruthy();
      expect(component.isEditMode).toBe(false);
    });

    it('should initialize with empty form in create mode', () => {
      expect(component.templateForm.value).toEqual({
        positionName: '',
        description: '',
      });
    });

    it('should have valid form with only positionName', () => {
      component.templateForm.patchValue({
        positionName: 'Developer',
      });
      expect(component.templateForm.valid).toBe(true);
    });

    it('should call createTemplate on save in create mode', () => {
      templateService.createTemplate.and.returnValue(of(mockTemplate));

      component.templateForm.patchValue({
        positionName: 'Developer',
        description: 'Description',
      });

      component.onSave();

      expect(templateService.createTemplate).toHaveBeenCalledWith({
        positionName: 'Developer',
        description: 'Description',
      });
    });

    it('should close dialog with created template', () => {
      templateService.createTemplate.and.returnValue(of(mockTemplate));

      component.templateForm.patchValue({
        positionName: 'Developer',
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(mockTemplate);
    });

    it('should handle error when creating template', () => {
      templateService.createTemplate.and.returnValue(
        throwError(() => ({ error: { message: 'Error creating' } }))
      );

      component.templateForm.patchValue({
        positionName: 'Developer',
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error creating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Edit Mode', () => {
    let component: TemplateDialogComponent;
    let fixture: ComponentFixture<TemplateDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplate',
        'updateTemplate',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TemplateDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'edit', template: mockTemplate } },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;

      fixture = TestBed.createComponent(TemplateDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create in edit mode', () => {
      expect(component).toBeTruthy();
      expect(component.isEditMode).toBe(true);
    });

    it('should initialize form with template data in edit mode', () => {
      expect(component.templateForm.value).toEqual({
        positionName: mockTemplate.positionName,
        description: mockTemplate.description,
      });
    });

    it('should call updateTemplate on save in edit mode', () => {
      templateService.updateTemplate.and.returnValue(of(mockTemplate));

      component.templateForm.patchValue({
        positionName: 'Senior Developer',
        description: 'Updated description',
      });

      component.onSave();

      expect(templateService.updateTemplate).toHaveBeenCalledWith(mockTemplate.id, {
        positionName: 'Senior Developer',
        description: 'Updated description',
      });
    });

    it('should close dialog with updated template', () => {
      const updatedTemplate = { ...mockTemplate, positionName: 'Senior Developer' };
      templateService.updateTemplate.and.returnValue(of(updatedTemplate));

      component.templateForm.patchValue({
        positionName: 'Senior Developer',
      });

      component.onSave();

      expect(dialogRef.close).toHaveBeenCalledWith(updatedTemplate);
    });

    it('should handle error when updating template', () => {
      templateService.updateTemplate.and.returnValue(
        throwError(() => ({ error: { message: 'Error updating' } }))
      );

      component.templateForm.patchValue({
        positionName: 'Senior Developer',
      });

      component.onSave();

      expect(component.errorMessage()).toBe('Error updating');
      expect(component.isSaving()).toBe(false);
    });
  });

  describe('Form Validation', () => {
    let component: TemplateDialogComponent;
    let fixture: ComponentFixture<TemplateDialogComponent>;
    let templateService: jasmine.SpyObj<TemplateService>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplate',
        'updateTemplate',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TemplateDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
      fixture = TestBed.createComponent(TemplateDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be invalid when positionName is empty', () => {
      component.templateForm.patchValue({ positionName: '' });
      expect(component.templateForm.invalid).toBe(true);
    });

    it('should be valid when positionName is provided', () => {
      component.templateForm.patchValue({ positionName: 'Developer' });
      expect(component.templateForm.valid).toBe(true);
    });

    it('should not submit invalid form', () => {
      component.templateForm.patchValue({ positionName: '' });
      component.onSave();

      expect(templateService.createTemplate).not.toHaveBeenCalled();
      expect(component.templateForm.touched).toBe(true);
    });

    it('should return error message for required field', () => {
      const control = component.templateForm.get('positionName');
      control?.setValue('');
      control?.markAsTouched();

      const errorMsg = component.getErrorMessage('positionName');
      expect(errorMsg).toBe('To pole jest wymagane');
    });

    it('should return empty string when field is valid', () => {
      component.templateForm.get('positionName')?.setValue('Developer');
      const errorMsg = component.getErrorMessage('positionName');
      expect(errorMsg).toBe('');
    });

    it('should return empty string when field is not touched', () => {
      component.templateForm.get('positionName')?.setValue('');
      const errorMsg = component.getErrorMessage('positionName');
      expect(errorMsg).toBe('');
    });
  });

  describe('onCancel', () => {
    let component: TemplateDialogComponent;
    let fixture: ComponentFixture<TemplateDialogComponent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplate',
        'updateTemplate',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TemplateDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<TemplateDialogComponent>>;
      fixture = TestBed.createComponent(TemplateDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should close dialog without data', () => {
      component.onCancel();
      expect(dialogRef.close).toHaveBeenCalledWith();
    });
  });

  describe('Component Rendering', () => {
    let component: TemplateDialogComponent;
    let fixture: ComponentFixture<TemplateDialogComponent>;

    beforeEach(async () => {
      const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
        'createTemplate',
        'updateTemplate',
      ]);
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [TemplateDialogComponent, ReactiveFormsModule],
        providers: [
          { provide: TemplateService, useValue: templateServiceSpy },
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TemplateDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should render form', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const form = compiled.querySelector('form');
      expect(form).toBeTruthy();
    });

    it('should render positionName input', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const input = compiled.querySelector('input[formControlName="positionName"]');
      expect(input).toBeTruthy();
    });

    it('should render description textarea', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const textarea = compiled.querySelector('textarea[formControlName="description"]');
      expect(textarea).toBeTruthy();
    });
  });
});
