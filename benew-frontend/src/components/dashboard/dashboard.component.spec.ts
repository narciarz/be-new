import { ComponentFixture, TestBed } from '@angular/core/testing';
import {Router, ActivatedRoute, NavigationEnd} from '@angular/router';
import { signal } from '@angular/core';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../services';
import { UserRole } from '../../models';
import { provideAnimations } from '@angular/platform-browser/animations';
import {of} from 'rxjs';

function getRouterSpy() {
  const routerSpy = jasmine.createSpyObj('Router', [
    'navigate',
    'createUrlTree',
    'serializeUrl',
  ]);

  // Fix for "createUrlTree is not a function"
  routerSpy.createUrlTree.and.returnValue({});
  routerSpy.serializeUrl.and.returnValue('#');

  // Fix for "subscribe of undefined" (RouterLinkActive)
  (routerSpy as any).events = of(new NavigationEnd(0, 'url', 'url'));

  return routerSpy;
}

describe('DashboardComponent', () => {
  const mockUser = {
    id: '1',
    email: 'test@example.com',
    role: UserRole.USER,
    firstName: 'Test',
    lastName: 'User',
    token: 'test-token',
  };

  describe('Basic Functionality', () => {
    let component: DashboardComponent;
    let fixture: ComponentFixture<DashboardComponent>;
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
      const authServiceSpy = jasmine.createSpyObj('AuthService', ['logout'], {
        currentUser: signal(mockUser),
      });

      const routerSpy = getRouterSpy();
      const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
        snapshot: { params: {}, queryParams: {} },
      });

      await TestBed.configureTestingModule({
        imports: [DashboardComponent],
        providers: [
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: activatedRouteSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
      router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
      fixture = TestBed.createComponent(DashboardComponent);
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

    it('should call authService.logout', () => {
      component.onLogout();
      expect(authService.logout).toHaveBeenCalled();
    });

    it('should navigate to login page after logout', () => {
      component.onLogout();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should navigate to login page even if logout has no side effects', () => {
      authService.logout.and.stub();
      component.onLogout();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should render toolbar', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const toolbar = compiled.querySelector('mat-toolbar');
      expect(toolbar).toBeTruthy();
    });

    it('should render router outlet', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const routerOutlet = compiled.querySelector('router-outlet');
      expect(routerOutlet).toBeTruthy();
    });

    it('should work with USER role', () => {
      expect(component.currentUser()?.role).toBe(UserRole.USER);
    });
  });

  describe('Different User Roles - ADMIN', () => {
    let component: DashboardComponent;
    let fixture: ComponentFixture<DashboardComponent>;

    beforeEach(async () => {
      const adminUser = { ...mockUser, role: UserRole.ADMIN };
      const adminAuthService = jasmine.createSpyObj('AuthService', ['logout'], {
        currentUser: signal(adminUser),
      });
      const routerSpy = getRouterSpy();
      const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
        snapshot: { params: {}, queryParams: {} },
      });

      await TestBed.configureTestingModule({
        imports: [DashboardComponent],
        providers: [
          { provide: AuthService, useValue: adminAuthService },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: activatedRouteSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should work with ADMIN user', () => {
      expect(component.currentUser()?.role).toBe(UserRole.ADMIN);
    });
  });

  describe('Different User Roles - MANAGER', () => {
    let component: DashboardComponent;
    let fixture: ComponentFixture<DashboardComponent>;

    beforeEach(async () => {
      const managerUser = { ...mockUser, role: UserRole.MANAGER };
      const managerAuthService = jasmine.createSpyObj('AuthService', ['logout'], {
        currentUser: signal(managerUser),
      });
      const routerSpy = getRouterSpy();
      const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
        snapshot: { params: {}, queryParams: {} },
      });

      await TestBed.configureTestingModule({
        imports: [DashboardComponent],
        providers: [
          { provide: AuthService, useValue: managerAuthService },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: activatedRouteSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should work with MANAGER user', () => {
      expect(component.currentUser()?.role).toBe(UserRole.MANAGER);
    });
  });

  describe('Null User Handling', () => {
    let component: DashboardComponent;
    let fixture: ComponentFixture<DashboardComponent>;

    beforeEach(async () => {
      const nullAuthService = jasmine.createSpyObj('AuthService', ['logout'], {
        currentUser: signal(null),
      });
      const routerSpy = getRouterSpy();
      const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
        snapshot: { params: {}, queryParams: {} },
      });

      await TestBed.configureTestingModule({
        imports: [DashboardComponent],
        providers: [
          { provide: AuthService, useValue: nullAuthService },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: activatedRouteSpy },
          provideAnimations(),
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should handle null currentUser', () => {
      expect(component.currentUser()).toBeNull();
    });
  });
});
