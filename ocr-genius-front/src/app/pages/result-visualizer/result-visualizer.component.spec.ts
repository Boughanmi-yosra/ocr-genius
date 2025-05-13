import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultVisualizerComponent } from './result-visualizer.component';

describe('ResultVisualizerComponent', () => {
  let component: ResultVisualizerComponent;
  let fixture: ComponentFixture<ResultVisualizerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResultVisualizerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResultVisualizerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
