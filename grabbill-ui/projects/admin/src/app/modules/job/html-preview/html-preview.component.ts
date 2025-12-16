import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { getUpdatedMjmlHtml } from '@grabbill/lib';

@Component({
  selector: 'grabbill-admin-html-preview',
  templateUrl: './html-preview.component.html',
  styleUrls: ['./html-preview.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HtmlPreviewComponent implements AfterViewInit {
  _isOnLoad = false;
  _html!: string;

  @ViewChild('iframe') iframe!: ElementRef;

  @Input('html')
  set html(value: string) {
    this._html = value;

    if (this._isOnLoad) {
      this.setIframeReady(this.iframe, this._html!);
    }
  }

  onLoad() {
    this._isOnLoad = true;
    this.setIframeReady(this.iframe, this._html!);
  }

  private setIframeReady(iframe: ElementRef, html: string): void {
    if (iframe) {
      const iframeElement = iframe.nativeElement;
      const win: Window = iframeElement.contentWindow;

      const doc: Document = win.document;
      doc.open();
      doc.write(html);
      doc.close();

      // delay to wait image loaded
      setTimeout(() => {
        iframeElement.width = iframeElement.contentWindow.document.body.scrollWidth;
        iframeElement.height = iframeElement.contentWindow.document.body.scrollHeight;
      }, 500);
    }
  }

  getPreview(mjml: string) {
    return getUpdatedMjmlHtml(mjml);
  }

  ngAfterViewInit(): void {
    this.setIframeReady(this.iframe, this._html!);
  }
}
