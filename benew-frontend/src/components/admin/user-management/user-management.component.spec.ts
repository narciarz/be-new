import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { UserManagementComponent } from './user-management.component';
import { UserService } from '../../../services/user.service';
import { UserDto } from '../../../models/user.dto';
import { UserRole } from '../../../models/user-role';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let userService: jasmine.SpyObj<UserService>;
  let dialog: jasmine.SpyObj<MatDialog>;

  const mockUsers: UserDto[] = [
    {
      id: 'user-1',
      email: 'user1@example.com',
      firstName: 'John',
      lastName: 'Doe',
      positionName: 'developer',
      role: UserRole.USER,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    },
    {
      id: 'user-2',
      email: 'user2@example.com',
      firstName: 'Jane',
      lastName: 'Smith',
      positionName: 'manager',
      role: UserRole.MANAGER,
      createdAt: '2024-01-03T00:00:00Z',
      updatedAt: '2024-01-04T00:00:00Z',
    },
    {
      id: 'user-3',
      email: 'admin@example.com',
      firstName: 'Admin',
      lastName: 'User',
      positionName: 'admin',
      role: UserRole.ADMIN,
      createdAt: '2024-01-05T00:00:00Z',
      updatedAt: '2024-01-06T00:00:00Z',
    },
  ];

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getUsers',
      'createUser',
      'updateUser',
      'deleteUser',
    ]);
    const dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [UserManagementComponent],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        provideAnimations(),
      ],
    }).compileComponents();

    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    dialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
  });

  beforeEach(() => {
    userService.getUsers.and.returnValue(
      of({
        content: mockUsers,
        totalElements: 3,
        totalPages: 1,
        size: 100,
        number: 0,
      })
    );

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading state', () => {
    expect(component.isLoading()).toBe(true);
  });

  it('should load users on init', () => {
    fixture.detectChanges();

    expect(userService.getUsers).toHaveBeenCalledWith(0, 100);
    expect(component.allUsers()).toEqual(mockUsers);
    expect(component.isLoading()).toBe(false);
  });

  it('should display all users when no search query', () => {
    fixture.detectChanges();

    expect(component.users()).toEqual(mockUsers);
  });

  describe('Search Functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should filter users by email', () => {
      const event = { target: { value: 'user1' } } as any;
      component.onSearchChange(event);

      const filtered = component.users();
      expect(filtered.length).toBe(1);
      expect(filtered[0].email).toBe('user1@example.com');
    });

    it('should filter users by first name', () => {
      const event = { target: { value: 'Jane' } } as any;
      component.onSearchChange(event);

      const filtered = component.users();
      expect(filtered.length).toBe(1);
      expect(filtered[0].firstName).toBe('Jane');
    });

    it('should filter users by last name', () => {
      const event = { target: { value: 'Doe' } } as any;
      component.onSearchChange(event);

      const filtered = component.users();
      expect(filtered.length).toBe(1);
      expect(filtered[0].lastName).toBe('Doe');
    });

    it('should be case insensitive', () => {
      const event = { target: { value: 'JANE' } } as any;
      component.onSearchChange(event);

      const filtered = component.users();
      expect(filtered.length).toBe(1);
      expect(filtered[0].firstName).toBe('Jane');
    });

    it('should trim whitespace from search query', () => {
      const event = { target: { value: '  Jane  ' } } as any;
      component.onSearchChange(event);

      const filtered = component.users();
      expect(filtered.length).toBe(1);
    });

    it('should show all users when search query is cleared', () => {
      // First filter
      component.onSearchChange({ target: { value: 'Jane' } } as any);
      expect(component.users().length).toBe(1);

      // Then clear
      component.onSearchChange({ target: { value: '' } } as any);
      expect(component.users().length).toBe(3);
    });

    it('should return empty array when no match found', () => {
      const event = { target: { value: 'nonexistent' } } as any;
      component.onSearchChange(event);

      expect(component.users().length).toBe(0);
    });
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      spyOn(console, 'error'); // Suppress expected error logs
    });

    it('should handle error when loading users', () => {
      userService.getUsers.and.returnValue(
        throwError(() => new Error('Failed to load'))
      );

      fixture.detectChanges();

      expect(component.errorMessage()).toBe('Błąd podczas ładowania użytkowników');
      expect(component.isLoading()).toBe(false);
    });
  });

  describe('onAddUser', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open dialog for creating user', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddUser();

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should add new user to list when dialog returns result', () => {
      const newUser: UserDto = {
        id: 'user-4',
        email: 'new@example.com',
        firstName: 'New',
        lastName: 'User',
        positionName: 'developer',
        role: UserRole.USER,
        createdAt: '2024-01-07T00:00:00Z',
        updatedAt: '2024-01-08T00:00:00Z',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(newUser));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onAddUser();

      expect(component.allUsers().length).toBe(4);
      expect(component.allUsers()[3].id).toBe('user-4');
    });

    it('should not add user when dialog is cancelled', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      const initialLength = component.allUsers().length;
      component.onAddUser();

      expect(component.allUsers().length).toBe(initialLength);
    });
  });

  describe('onEditUser', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open dialog for editing user', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditUser('user-1');

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should update user in list when dialog returns result', () => {
      const updatedUser: UserDto = {
        ...mockUsers[0],
        firstName: 'Updated',
      };

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(updatedUser));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditUser('user-1');

      const user = component.allUsers().find((u) => u.id === 'user-1');
      expect(user?.firstName).toBe('Updated');
    });

    it('should return early if user not found', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onEditUser('non-existent');

      expect(dialog.open).not.toHaveBeenCalled();
    });
  });

  describe('onDeleteUser', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open confirmation dialog before deleting', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(false));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onDeleteUser('user-1');

      expect(dialog.open).toHaveBeenCalled();
    });

    it('should not delete user if confirmation cancelled', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(false));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onDeleteUser('user-1');

      expect(userService.deleteUser).not.toHaveBeenCalled();
    });

    it('should delete user when confirmed', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(true));
      dialog.open.and.returnValue(dialogRefSpy);
      userService.deleteUser.and.returnValue(of(void 0));

      component.onDeleteUser('user-1');

      expect(userService.deleteUser).toHaveBeenCalledWith('user-1');
    });

    it('should remove user from list after successful deletion', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(true));
      dialog.open.and.returnValue(dialogRefSpy);
      userService.deleteUser.and.returnValue(of(void 0));

      component.onDeleteUser('user-1');

      const user = component.allUsers().find((u) => u.id === 'user-1');
      expect(user).toBeUndefined();
      expect(component.allUsers().length).toBe(2);
    });

    it('should handle error when deleting user', () => {
      spyOn(console, 'error'); // Suppress expected error logs

      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(true));
      dialog.open.and.returnValue(dialogRefSpy);
      userService.deleteUser.and.returnValue(
        throwError(() => ({ error: { message: 'Cannot delete user' } }))
      );

      component.onDeleteUser('user-1');

      expect(component.errorMessage()).toBe('Cannot delete user');
    });

    it('should return early if user not found', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(true));
      dialog.open.and.returnValue(dialogRefSpy);

      component.onDeleteUser('non-existent');

      expect(dialog.open).not.toHaveBeenCalled();
    });
  });

  describe('onResetPassword', () => {
    beforeEach(() => {
      fixture.detectChanges();
      spyOn(window, 'alert');
    });

    it('should show alert for unimplemented feature', () => {
      component.onResetPassword('user-1');
      expect(window.alert).toHaveBeenCalledWith('Funkcja resetowania hasła będzie dostępna wkrótce');
    });
  });

  describe('displayedColumns', () => {
    it('should have correct column configuration', () => {
      expect(component.displayedColumns).toEqual([
        'email',
        'firstName',
        'lastName',
        'role',
        'actions',
      ]);
    });
  });

  describe('Component Rendering', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should render mat-card', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const card = compiled.querySelector('mat-card');
      expect(card).toBeTruthy();
    });

    it('should render mat-table', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const table = compiled.querySelector('table');
      expect(table).toBeTruthy();
    });

    it('should render search input', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const searchInput = compiled.querySelector('input');
      expect(searchInput).toBeTruthy();
    });

  });

  describe('Filtered Users with Search', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should update displayed users when search changes', () => {
      // Initial state
      expect(component.users().length).toBe(3);

      // Apply filter
      component.searchQuery.set('Jane');
      expect(component.users().length).toBe(1);
      expect(component.users()[0].firstName).toBe('Jane');
    });

    it('should filter from allUsers, not from already filtered list', () => {
      // First search
      component.searchQuery.set('Jane');
      expect(component.users().length).toBe(1);

      // Second search should search all users, not just Jane
      component.searchQuery.set('John');
      expect(component.users().length).toBe(1);
      expect(component.users()[0].firstName).toBe('John');
    });
  });
});
