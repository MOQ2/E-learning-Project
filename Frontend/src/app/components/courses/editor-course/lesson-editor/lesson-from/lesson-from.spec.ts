import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LessonFrom } from './lesson-from';

describe('LessonFrom', () => {
  let component: LessonFrom;
  let fixture: ComponentFixture<LessonFrom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LessonFrom]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LessonFrom);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
