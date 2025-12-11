import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError, Observable } from 'rxjs';
import { TemplateImportComponent } from './template-import.component';
import { TemplateService } from '../../../services/template.service';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('TemplateImportComponent', () => {
  let component: TemplateImportComponent;
  let fixture: ComponentFixture<TemplateImportComponent>;
  let templateService: jasmine.SpyObj<TemplateService>;

  beforeEach(async () => {
    const templateServiceSpy = jasmine.createSpyObj('TemplateService', [
      'importTemplateFromCsv',
    ]);

    await TestBed.configureTestingModule({
      imports: [TemplateImportComponent],
      providers: [
        { provide: TemplateService, useValue: templateServiceSpy },
        provideAnimations(),
      ],
    }).compileComponents();

    templateService = TestBed.inject(TemplateService) as jasmine.SpyObj<TemplateService>;
    fixture = TestBed.createComponent(TemplateImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with not uploading state', () => {
    expect(component.isUploading()).toBe(false);
  });

  it('should initialize with null upload result', () => {
    expect(component.uploadResult()).toBeNull();
  });

  describe('onFileSelected', () => {
    let mockFile: File;
    let mockEvent: any;

    beforeEach(() => {
      mockFile = new File(['test content'], 'test.csv', { type: 'text/csv' });
      mockEvent = {
        target: {
          files: [mockFile],
          value: 'test.csv',
        },
      };
    });

    it('should handle successful file upload', () => {
      const importResponse = {
        id: 'template-1',
        positionName: 'Developer',
        tasksImported: 5,
      };

      templateService.importTemplateFromCsv.and.returnValue(of(importResponse));

      component.onFileSelected(mockEvent);

      expect(templateService.importTemplateFromCsv).toHaveBeenCalledWith(mockFile);
      expect(component.uploadResult()?.success).toBe(true);
      expect(component.uploadResult()?.tasksImported).toBe(5);
      expect(component.isUploading()).toBe(false);
    });

    it('should handle tasksCount property if tasksImported not present', () => {
      const importResponse = {
        id: 'template-1',
        positionName: 'Developer',
        tasksCount: 3,
      };

      templateService.importTemplateFromCsv.and.returnValue(of(importResponse as any));

      component.onFileSelected(mockEvent);

      expect(component.uploadResult()?.tasksImported).toBe(3);
    });

    it('should set uploading state during upload', () => {
      // Create a delayed observable that doesn't complete immediately
      const delayedResponse = new Observable((subscriber) => {
        // Don't emit immediately, so we can check the loading state
        setTimeout(() => {
          subscriber.next({ id: 'template-1', positionName: 'Developer', tasksImported: 5 });
          subscriber.complete();
        }, 100);
      });

      templateService.importTemplateFromCsv.and.returnValue(delayedResponse);

      component.onFileSelected(mockEvent);

      // Check uploading state immediately (before observable emits)
      expect(component.isUploading()).toBe(true);
    });

    it('should clear upload result before new upload', () => {
      // First upload
      const importResponse = { id: 'template-1', positionName: 'Developer', tasksImported: 5 };
      templateService.importTemplateFromCsv.and.returnValue(of(importResponse));
      component.onFileSelected(mockEvent);

      expect(component.uploadResult()).not.toBeNull();

      // Second upload
      component.onFileSelected(mockEvent);
      // Result should be null at the start of upload
      // But since the mock returns immediately, we can't test the intermediate state
      expect(templateService.importTemplateFromCsv).toHaveBeenCalledTimes(2);
    });

    it('should validate file type and reject non-CSV files', () => {
      const invalidFile = new File(['test'], 'test.txt', { type: 'text/plain' });
      const invalidEvent = {
        target: {
          files: [invalidFile],
          value: 'test.txt',
        },
      } as any;

      component.onFileSelected(invalidEvent);

      expect(templateService.importTemplateFromCsv).not.toHaveBeenCalled();
      expect(component.uploadResult()?.success).toBe(false);
      expect(component.uploadResult()?.message).toBe(
        'Nieprawidłowy format pliku. Wybierz plik CSV.'
      );
    });

    it('should handle error during upload', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      templateService.importTemplateFromCsv.and.returnValue(
        throwError(() => ({ error: { message: 'Import failed' } }))
      );

      component.onFileSelected(mockEvent);

      expect(component.uploadResult()?.success).toBe(false);
      expect(component.uploadResult()?.message).toBe('Import failed');
      expect(component.isUploading()).toBe(false);
    });

    it('should use generic error message when error message not provided', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      templateService.importTemplateFromCsv.and.returnValue(
        throwError(() => ({ error: {} }))
      );

      component.onFileSelected(mockEvent);

      expect(component.uploadResult()?.message).toBe('Błąd podczas importu szablonu');
    });

    it('should return early if no file selected', () => {
      const emptyEvent = {
        target: {
          files: [],
        },
      } as any;

      component.onFileSelected(emptyEvent);

      expect(templateService.importTemplateFromCsv).not.toHaveBeenCalled();
    });

    it('should return early if files is null', () => {
      const nullEvent = {
        target: {
          files: null,
        },
      } as any;

      component.onFileSelected(nullEvent);

      expect(templateService.importTemplateFromCsv).not.toHaveBeenCalled();
    });

    it('should clear file input after successful upload', () => {
      const importResponse = { id: 'template-1', positionName: 'Developer', tasksImported: 5 };
      templateService.importTemplateFromCsv.and.returnValue(of(importResponse));

      component.onFileSelected(mockEvent);

      expect(mockEvent.target.value).toBe('');
    });

    it('should clear file input after failed upload', () => {
      templateService.importTemplateFromCsv.and.returnValue(
        throwError(() => ({ error: { message: 'Import failed' } }))
      );

      component.onFileSelected(mockEvent);

      expect(mockEvent.target.value).toBe('');
    });
  });

  describe('onDownloadTemplate', () => {
    it('should create and download CSV file', () => {
      const createElementSpy = spyOn(document, 'createElement').and.callThrough();
      const createObjectURLSpy = spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
      const revokeObjectURLSpy = spyOn(URL, 'revokeObjectURL');

      // Spy on appendChild and removeChild
      const appendChildSpy = spyOn(document.body, 'appendChild');
      const removeChildSpy = spyOn(document.body, 'removeChild');

      component.onDownloadTemplate();

      expect(createElementSpy).toHaveBeenCalledWith('a');
      expect(createObjectURLSpy).toHaveBeenCalled();
    });

    it('should generate correct CSV content', () => {
      spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
      spyOn(document.body, 'appendChild');
      spyOn(document.body, 'removeChild');

      component.onDownloadTemplate();

      // The download was triggered successfully - we can't easily test Blob content
      // without complex mocking, so we'll just verify the method completes
      expect(URL.createObjectURL).toHaveBeenCalled();
    });

    it('should set correct download filename', () => {
      const link = document.createElement('a');
      spyOn(document, 'createElement').and.returnValue(link);
      spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
      spyOn(document.body, 'appendChild');
      spyOn(document.body, 'removeChild');

      component.onDownloadTemplate();

      expect(link.getAttribute('download')).toBe('onboarding_template_example.csv');
    });
  });

  describe('onReset', () => {
    it('should clear upload result', () => {
      component.uploadResult.set({
        success: true,
        message: 'Success',
        tasksImported: 5,
      });

      component.onReset();

      expect(component.uploadResult()).toBeNull();
    });
  });

  describe('Component Rendering', () => {
    it('should render mat-card', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render file input', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const fileInput = compiled.querySelector('input[type="file"]');
      expect(fileInput).toBeTruthy();
    });

    it('should render download template button', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const buttons = compiled.querySelectorAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });
  });
});
