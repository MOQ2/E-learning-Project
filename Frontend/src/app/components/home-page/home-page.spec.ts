import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomePage } from './home-page';
import { NavBar } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';

describe('HomePage', () => {
  let component: HomePage;
  let fixture: ComponentFixture<HomePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomePage, NavBar, Footer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
