import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FetchQuizzes } from './fetch-quizzes';

describe('FetchQuizzes', () => {
  let component: FetchQuizzes;
  let fixture: ComponentFixture<FetchQuizzes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FetchQuizzes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FetchQuizzes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
