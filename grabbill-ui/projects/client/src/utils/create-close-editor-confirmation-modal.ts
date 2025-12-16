import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';

export const createCloseEditorConfirmationModal = (modal: NzModalService, saveCallback: () => void, closeEditor: () => void) => {
  const editorModal: NzModalRef = modal.create({
    nzTitle: `Save Changes`,
    nzContent: 'Save or discard template changes?',
    nzFooter: [
      {
        label: 'Save Changes',
        type: 'primary',
        onClick: () => {
          saveCallback();
          editorModal.destroy();
        },
      },
      {
        label: 'Discard Changes',
        onClick: () => {
          closeEditor();
          editorModal.destroy();
        },
      },
      {
        label: 'Cancel',
        onClick: () => {
          editorModal.destroy();
        },
      },
    ],
    nzOnCancel: () => {
      editorModal.destroy();
    },
  });

  editorOutOfFocus();
};

export const editorOutOfFocus = () => {
  window.scrollTo(window.scrollX, window.scrollY - 1);
  window.dispatchEvent(new KeyboardEvent('keydown', {'key': 'a'}))
}
