import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InfoComponent } from './info.component';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('InfoComponent', () => {
  let component: InfoComponent;
  let fixture: ComponentFixture<InfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InfoComponent],
      providers: [provideAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(InfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Rendering', () => {
    it('should render mat-card', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render mat-expansion-panel', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const expansionPanel = compiled.querySelector('mat-expansion-panel');
      expect(expansionPanel).toBeTruthy();
    });
  });

  describe('Static Content', () => {
    it('should be a simple component without dynamic behavior', () => {
      // This component is static, so we just verify it renders without errors
      expect(component).toBeTruthy();
      expect(fixture.nativeElement).toBeTruthy();
    });
  });
});
