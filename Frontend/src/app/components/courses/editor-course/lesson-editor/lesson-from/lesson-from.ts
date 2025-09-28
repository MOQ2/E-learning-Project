import { Component, Input, Output, EventEmitter, ViewEncapsulation, OnChanges, AfterViewInit, ElementRef } from '@angular/core';
import { Lesson } from '../lesson-page/lesson.model';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';

// Declare Quill to avoid TypeScript errors.
declare var Quill: any;

@Component({
  selector: 'app-lesson-editor',
  templateUrl: './lesson-from.html',
  styleUrl: './lesson-from.css',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  encapsulation: ViewEncapsulation.None
})
export class LessonEditorComponent implements OnChanges, AfterViewInit {

  @Input() lesson!: Lesson;

  @Output() saveLesson = new EventEmitter<Lesson>();
  @Output() saveDraft = new EventEmitter<Lesson>();
  @Output() preview = new EventEmitter<Lesson>();
  @Output() deleteAttachment = new EventEmitter<{lessonId: number, attachmentId: number}>();
  @Output() goBack = new EventEmitter<void>();

  lessonForm!: FormGroup;
  private quillEditor: any;
  selectedFiles: (File | null)[] = [];

  constructor(private fb: FormBuilder, private elementRef: ElementRef) {
    this.initForm();
  }

  ngOnChanges() {
    if (this.lesson) {
      this.populateForm();
      this.lesson.state = this.lesson.id ? 'saved' : 'new';

      this.lessonForm.valueChanges.subscribe(() => {
        if (this.lesson.state !== 'new') {
          this.lesson.state = 'edited';
        }
      });
    }
  }

  ngAfterViewInit(): void {
    const editorElement = this.elementRef.nativeElement.querySelector('#editor-container');
    if (editorElement) {
      this.quillEditor = new Quill(editorElement, {
        theme: 'snow',
        modules: {
          toolbar: [
            [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ 'list': 'ordered' }, { 'list': 'bullet' }],
            [{ 'indent': '-1' }, { 'indent': '+1' }],
            [{ 'align': [] }],
            ['link', 'image', 'video'],
            ['clean']
          ]
        }
      });

      // Set initial content from form if available
      const initialContent = this.lessonForm.get('explanation')?.value;
      if (initialContent) {
        this.quillEditor.root.innerHTML = initialContent;
      }

      // Listen for changes and update the form control
      this.quillEditor.on('text-change', () => {
        const html = this.quillEditor.root.innerHTML;
        // Avoid setting value on initial load if it's just an empty paragraph
        if (html !== '<p><br></p>' || this.lessonForm.get('explanation')?.value) {
          this.lessonForm.get('explanation')?.setValue(html, { emitEvent: false });
        }
      });
    }
  }

  private initForm(): void {
    this.lessonForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      explanation: ['', [Validators.required, Validators.minLength(10)]],
      order: [0, [Validators.required, Validators.min(0)]],
      duration: [1, [Validators.required, Validators.min(1)]],
      status: ['Active'],
      whatWeWillLearn: this.fb.array([new FormControl('', Validators.required)]),
      prerequisites: this.fb.array([new FormControl('', Validators.required)]),
      attachments: this.fb.array([])
    });
  }

  private populateForm(): void {
    if (this.lesson) {
      this.lessonForm.patchValue({
        title: this.lesson.title,
        explanation: this.lesson.explanation,
        order: this.lesson.order,
        duration: Math.floor(this.lesson.duration / 60),
        status: this.lesson.status
      });
      // If editor is initialized, update its content
      if (this.quillEditor) {
        this.quillEditor.root.innerHTML = this.lesson.explanation || '';
      }
      this.setFormArray('whatWeWillLearn', this.lesson.whatWeWillLearn);
      this.setFormArray('prerequisites', this.lesson.prerequisites);
      this.attachments.clear();
      this.selectedFiles = [];
      this.lesson.attachments.forEach(att => {
        this.attachments.push(this.fb.group({
          id: [att.id],
          displayName: [att.displayName]
        }));
        this.selectedFiles.push(att.file);
      });
    }
  }

  private setFormArray(arrayName: 'whatWeWillLearn' | 'prerequisites', values: string[]): void {
    const formArray = this.lessonForm.get(arrayName) as FormArray;
    formArray.clear();
    if (values && values.length > 0) {
        values.forEach(value => {
            formArray.push(new FormControl(value || '', Validators.required));
        });
    } else {
        formArray.push(new FormControl('', Validators.required)); // Ensure there's at least one field
    }
  }


  get whatWeWillLearnControls() {
    return (this.lessonForm.get('whatWeWillLearn') as FormArray).controls;
  }

  get prerequisitesControls() {
    return (this.lessonForm.get('prerequisites') as FormArray).controls;
  }

  get attachments() {
    return this.lessonForm.get('attachments') as FormArray;
  }

  onFormSubmit() {
    if (this.lessonForm.valid && (this.lesson.state === 'edited' || this.lesson.state === 'new')) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60,
        attachments: formValue.attachments.map((att: any, idx: number) => ({ id: att.id, file: this.selectedFiles[idx], displayName: att.displayName }))
      };
      this.saveLesson.emit(lessonToSave);
      this.lesson.state = 'saved';
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onSaveDraftClick() {
    if (this.lessonForm.valid && (this.lesson.state === 'edited' || this.lesson.state === 'new')) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60,
        attachments: formValue.attachments.map((att: any, idx: number) => ({ id: att.id, file: this.selectedFiles[idx], displayName: att.displayName }))
      };
      this.saveDraft.emit(lessonToSave);
      this.lesson.state = 'saved';
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onPreviewClick() {
    if (this.lessonForm.valid) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60,
        attachments: formValue.attachments.map((att: any, idx: number) => ({ id: att.id, file: this.selectedFiles[idx], displayName: att.displayName }))
      };
      this.preview.emit(lessonToSave);
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onBackClick() {
    this.goBack.emit();
  }

  addBullet(list: 'whatWeWillLearn' | 'prerequisites') {
    const formArray = this.lessonForm.get(list) as FormArray;
    formArray.push(new FormControl('', Validators.required));
  }

  removeBullet(list: 'whatWeWillLearn' | 'prerequisites', index: number) {
    const formArray = this.lessonForm.get(list) as FormArray;
    if (formArray.length > 1) {
      formArray.removeAt(index);
    }
  }

  addAttachment() {
    this.attachments.push(this.fb.group({
      id: [null],
      displayName: ['']
    }));
    this.selectedFiles.push(null);
  }

  removeAttachment(index: number) {
    const attachment = this.attachments.at(index);
    const attachmentId = attachment.get('id')?.value;
    const lessonId = this.lesson.id;
    if (attachmentId && lessonId) {
      this.deleteAttachment.emit({ lessonId, attachmentId });
    }
    this.attachments.removeAt(index);
    this.selectedFiles.splice(index, 1);
  }

  onFileSelected(event: any, index: number) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[index] = file;
      const attachmentGroup = this.attachments.at(index);
      attachmentGroup.patchValue({ displayName: file.name });
    }
  }

  onVideoFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.lesson.videoData = file;
    }
  }

  onThumbnailFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.lesson.thumbnailFile = file;
    }
  }

  trackByIndex(index: number, item: any): any {
    return index;
  }
}
