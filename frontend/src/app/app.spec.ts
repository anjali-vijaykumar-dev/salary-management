import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('creates the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('shows navigation to employees and insights', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Employees');
    expect(text).toContain('Insights');
  });
});
