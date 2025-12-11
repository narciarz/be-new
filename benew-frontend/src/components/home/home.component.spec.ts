import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { HomeComponent } from './home.component';
import { AuthService } from '../../services';
import { UserRole } from '../../models';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('HomeComponent', () => {
  const mockUser = {
    id: '1',
    email: 'test@example.com',
    role: UserRole.USER,
    firstName: 'Test',
    lastName: 'User',
    token: 'test-token',
  };

  describe('Basic Functionality', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;
    let authService: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
      const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(mockUser),
      });

      await TestBed.configureTestingModule({
        imports: [HomeComponent],
        providers: [
          { provide: AuthService, useValue: authServiceSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
      fixture = TestBed.createComponent(HomeComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should have access to currentUser from AuthService', () => {
      expect(component.currentUser()).toEqual(mockUser);
    });

    it('should expose UserRole enum to template', () => {
      expect(component.UserRole).toEqual(UserRole);
    });

    it('should work with USER role', () => {
      expect(component.currentUser()?.role).toBe(UserRole.USER);
    });

    it('should render mat-card', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });
  });

  describe('Different User Roles - ADMIN', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;

    beforeEach(async () => {
      const adminUser = { ...mockUser, role: UserRole.ADMIN };
      const adminAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(adminUser),
      });

      await TestBed.configureTestingModule({
        imports: [HomeComponent],
        providers: [
          { provide: AuthService, useValue: adminAuthService },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(HomeComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should work with ADMIN user', () => {
      expect(component.currentUser()?.role).toBe(UserRole.ADMIN);
    });
  });

  describe('Different User Roles - MANAGER', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;

    beforeEach(async () => {
      const managerUser = { ...mockUser, role: UserRole.MANAGER };
      const managerAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(managerUser),
      });

      await TestBed.configureTestingModule({
        imports: [HomeComponent],
        providers: [
          { provide: AuthService, useValue: managerAuthService },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(HomeComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should work with MANAGER user', () => {
      expect(component.currentUser()?.role).toBe(UserRole.MANAGER);
    });
  });

  describe('Null User Handling', () => {
    let component: HomeComponent;
    let fixture: ComponentFixture<HomeComponent>;

    beforeEach(async () => {
      const nullAuthService = jasmine.createSpyObj('AuthService', [], {
        currentUser: signal(null),
      });

      await TestBed.configureTestingModule({
        imports: [HomeComponent],
        providers: [
          { provide: AuthService, useValue: nullAuthService },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(HomeComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle null currentUser', () => {
      expect(component.currentUser()).toBeNull();
    });
  });
});
