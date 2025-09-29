import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmitQuiz } from './submit-quiz';

describe('SubmitQuiz', () => {
  let component: SubmitQuiz;
  let fixture: ComponentFixture<SubmitQuiz>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubmitQuiz]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubmitQuiz);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
