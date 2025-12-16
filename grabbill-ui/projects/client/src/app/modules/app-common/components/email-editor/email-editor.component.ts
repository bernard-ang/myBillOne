import grapesjs, { Editor } from "grapesjs";
// @ts-ignore
import grapesJSMJML from 'grapesjs-mjml';
import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { BaseIndexFieldModel, RootImageFolderModel } from '@grabbill/lib';
import { Select, Store } from '@ngxs/store';
import { ImageState } from '../../../../../states/image/image.state';
import { Observable, tap } from 'rxjs';
import { GetRootImageFolder } from '../../../../../states/image/image.state-actions';
import { NgxsBaseComponent } from '../../../../components/ngxs-base.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getUpdatedMjmlHtml } from '../../../../../utils/get-updated-mjml-html';

@Component({
  selector: 'grabbill-client-email-editor',
  templateUrl: './email-editor.component.html',
  styleUrls: ['./email-editor.component.less'],
})
export class EmailEditorComponent extends NgxsBaseComponent implements AfterViewInit {
  @Input('defaultComponents')
  defaultComponents!: string;

  @Input('fields')
  mergeFields: BaseIndexFieldModel[] = [];

  @ViewChild('gjs') gjs!: ElementRef<HTMLDivElement>;

  @Output() htmlInit = new EventEmitter<string>();

  @Select(ImageState.rootFolder)
  rootFolder$!: Observable<RootImageFolderModel>;

  public grapesjsEditor?: Editor;

  constructor(protected override store: Store, protected override messageService: NzMessageService) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    this.autoUnsubscribe(
      this.rootFolder$.pipe(
        tap((rootFolder) => {
          if (rootFolder && this.grapesjsEditor) {
            this.addImages(rootFolder);
          }
        })
      )
    );

    this.store.dispatch(new GetRootImageFolder());
  }

  ngAfterViewInit(): void {
    try {
      this.grapesjsEditor = grapesjs.init({
        // Indicate where to init the editor. You can also pass an HTMLElement
        container: this.gjs.nativeElement,
        // Get the content for the canvas directly from the element
        // As an alternative we could use: `components: '<h1>Hello World Component!</h1>'`,
        fromElement: false,
        storageManager: false,
        components: this.defaultComponents,
        height: 'calc(100vh - 108px)',
        plugins: [grapesJSMJML],
        assetManager: { assets: [] },
      });

      const wrapper = this.grapesjsEditor.getWrapper();
      if (wrapper) {
        wrapper.toHTML = function (opts) {
          return this.getInnerHTML(opts);
        };
      }

      this.addUnsubscribeLink();
      this.addMergeFields();

      this.htmlInit.emit(getUpdatedMjmlHtml(this.grapesjsEditor.runCommand('mjml-code-to-html', {}).html));

      this.grapesjsEditor.Panels.getButton('views', 'open-blocks')!.set('active', true);
    } catch (e) {
      console.error(e);
    }
  }

  private addImages(rootFolder: RootImageFolderModel) {
    const assetManager = this.grapesjsEditor!.AssetManager;

    for (const folder of rootFolder.imageFolders) {
      for (const image of folder.images) {
        assetManager.add({
          type: 'image',
          src: image.externalUrl,
          name: `${folder.name}/${image.filename}`,
        });
      }
    }

    for (const image of rootFolder.images) {
      assetManager.add({
        type: 'image',
        src: image.externalUrl,
        name: image.filename,
      });
    }
  }

  private addUnsubscribeLink() {
    (this.grapesjsEditor!.Blocks as any).add('unsubscribe-link', {
      label: 'Unsubscribe link',
      content:
        '<mj-text align="center"><a href="{{unsubscribe_link}}" title="Unsubscribe Link" target="_blank">Unsubscribe</a></mj-text>',
      attributes: { class: 'fa fa-chain-broken' },
    });
  }

  private addMergeFields() {
    const fields = this.mergeFields.filter((field) => field.applicable);
    for (const mergeField of fields) {
      (this.grapesjsEditor!.Blocks as any).add(`merge-field-${mergeField.header}`, {
        label: mergeField.label,
        content: `<mj-text>{{${mergeField.header}}}</mj-text>`,
        attributes: { class: 'fa fa-hashtag' },
      });
    }
  }
}
