import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfirmationDialogComponent, ConfirmationDialogData } from './confirmation-dialog.component';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('ConfirmationDialogComponent', () => {
  const mockData: ConfirmationDialogData = {
    title: 'Test Title',
    message: 'Test Message',
    confirmText: 'Confirm',
    cancelText: 'Cancel',
    danger: false,
  };

  describe('Basic Functionality', () => {
    let component: ConfirmationDialogComponent;
    let fixture: ComponentFixture<ConfirmationDialogComponent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<ConfirmationDialogComponent>>;

    beforeEach(async () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: mockData },
          provideAnimations(),
        ],
      }).compileComponents();

      dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<ConfirmationDialogComponent>>;
      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should inject dialog data correctly', () => {
      expect(component.data).toEqual(mockData);
    });

    it('should close dialog with true on confirm', () => {
      component.onConfirm();
      expect(dialogRef.close).toHaveBeenCalledWith(true);
    });

    it('should close dialog with false on cancel', () => {
      component.onCancel();
      expect(dialogRef.close).toHaveBeenCalledWith(false);
    });
  });

  describe('Dialog Data Variations - Danger Mode', () => {
    let component: ConfirmationDialogComponent;
    let fixture: ComponentFixture<ConfirmationDialogComponent>;

    beforeEach(async () => {
      const dangerData: ConfirmationDialogData = {
        title: 'Danger!',
        message: 'This is dangerous',
        danger: true,
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: dangerData },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle danger dialog', () => {
      expect(component.data.danger).toBe(true);
    });

    it('should show warning icon for danger dialog', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const warningIcon = compiled.querySelector('.warning-icon');
      expect(warningIcon).toBeTruthy();
    });
  });

  describe('Dialog Data Variations - Default Texts', () => {
    let component: ConfirmationDialogComponent;
    let fixture: ComponentFixture<ConfirmationDialogComponent>;

    beforeEach(async () => {
      const dataWithoutTexts: ConfirmationDialogData = {
        title: 'Title',
        message: 'Message',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: dataWithoutTexts },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should use default confirm text when not provided', () => {
      expect(component.data.confirmText).toBeUndefined();
    });

    it('should use default cancel text when not provided', () => {
      expect(component.data.cancelText).toBeUndefined();
    });
  });

  describe('Component Rendering', () => {
    let component: ConfirmationDialogComponent;
    let fixture: ComponentFixture<ConfirmationDialogComponent>;

    beforeEach(async () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

      await TestBed.configureTestingModule({
        imports: [ConfirmationDialogComponent],
        providers: [
          { provide: MatDialogRef, useValue: dialogRefSpy },
          { provide: MAT_DIALOG_DATA, useValue: mockData },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmationDialogComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should render dialog title', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const title = compiled.querySelector('h2[mat-dialog-title]');
      expect(title).toBeTruthy();
      expect(title?.textContent).toContain(mockData.title);
    });

    it('should render dialog message', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const content = compiled.querySelector('mat-dialog-content p');
      expect(content).toBeTruthy();
    });

    it('should render cancel button', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const buttons = compiled.querySelectorAll('button');
      expect(buttons.length).toBeGreaterThanOrEqual(2);
    });

    it('should render confirm button', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const buttons = compiled.querySelectorAll('button');
      expect(buttons.length).toBeGreaterThanOrEqual(2);
    });

    it('should not show warning icon for normal dialog', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const warningIcon = compiled.querySelector('.warning-icon');
      expect(warningIcon).toBeFalsy();
    });
  });
});
