import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExploreCoursesPage } from './explore-courses';

describe('ExploreCourses', () => {
  let component: ExploreCoursesPage;
  let fixture: ComponentFixture<ExploreCoursesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExploreCoursesPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExploreCoursesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
