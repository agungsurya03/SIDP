/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import controller.importer.TkiSpreadsheet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import model.fresh.*;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Raihan
 */
public class GlobalBean {

    private static final long serialVersionUID = -1005091770846750945L;

    private BackendBrain backendBrain;

    private String role = "skk";
    //untuk histori halaman
    private int idUser = 0;
    private int idKkks;
    private int idUserkkks;
    private int idRptk;
    private int idRptkDefault;
    private int idStruktur;
    private int idHalaman;
    private int tmpIdHalaman;
    private int idJabatan;
    private int idPenggunaan;
    private int idPelatihan;
    private int idPertukaran;
    private int idTka;
    private int idTki;
    private Integer hstart;
    private Integer hend;

    //untuk cascading picker
    private int filterKkks;
    private int filterKkksDefault;
    private int filterRptk;
    private int filterRptkDefault;
    private int filterStruktur;
    private int filterHalaman;
    private int filterJabatan;

    //untuk di filter list view
    private String filterNamaKkks;
    private String filterNamaRptk;

    //untuk salin rptk
    private Rptk salinRptk;
    private String ketSalin;
    private Rptk rptkDefault;

    private String state = "detail";
    private boolean isHalamanOpened;
    private boolean isJabatanOpened;
    private boolean isPenggunaanOpened;

    private StreamedContent logokkks;

    private ArrayList<String> undone;

    Map<String, Object> pickerOptions;
    boolean sessionbumper;

    private String urlLogoKkks;

    public BackendBrain getBackendBrain() {
        return backendBrain;
    }

    public void setBackendBrain(BackendBrain backendBrain) {
        this.backendBrain = backendBrain;
    }

    public void showMessage(String alerton) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Confirmation", alerton);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public GlobalBean() {
        filterKkksDefault = -1;
        filterNamaKkks = "Pilih KKKS";
        filterRptkDefault = -1;//untuk di filter main view gunakan default
        filterNamaRptk = "Pilih RPTK";
    }

    public String getPathImta(String url) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        return phase2 + url;
    }

    @PostConstruct
    public void fillTools() {
        pickerOptions = new HashMap<String, Object>();
        pickerOptions.put("modal", true);
        pickerOptions.put("draggable", false);
        pickerOptions.put("resizable", false);
    }

    public void closeAll() {
        isHalamanOpened = false;
        isJabatanOpened = false;
        isPenggunaanOpened = false;
    }

    public void closePenggunaan() {
        isPenggunaanOpened = false;
    }

    public void closeJabatan() {
        isJabatanOpened = false;
    }

    public String manageKkks(int id) {
        idKkks = id;
        state = "detail";
//        if (backendBrain.metaKkksLogo(id).getFilename() != null) {
//            logokkks = backendBrain.fetchKkksLogo(id);
//        } else {
//            logokkks = null;
//        }
        return "manage";
    }

    public void openlogo(int id) {
        if (id > 0) {
            if (backendBrain.metaKkksLogo(id).getFilename() != null) {
                System.out.println("logoid: " + id);
                logokkks = backendBrain.fetchKkksLogo(id);
                System.out.println("logokkks: " + logokkks.getName());
            } else {
                logokkks = null;
            }
        }
    }

    public String manageUserkkks(int id) {
        idUserkkks = id;
        state = "detail";
        return "manage";
    }

    public String manageRptk(int id) {
        idRptk = id;
        state = "detail";
        ketSalin = "det";
        return "manage";
    }

    public void manageRptk2(int id, int idKkks) throws IOException {
        idRptk = id;
        filterKkksDefault = idKkks;
        state = "detail";
        ketSalin = "det";
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(ec.getRequestContextPath() + "/editView/rptk_manage.xhtml");
    }

    public String openDesainer(int id, boolean check, int idStruktur) {
        System.out.println(id);
        if(id == 0){
            Struktur struktur = backendBrain.fetchStrukturPK(idStruktur);
            Icon icon = backendBrain.fetchSIconFK(struktur.getIdStruktur());
            backendBrain.deleteIconPK(icon.getIdIcon());
            backendBrain.deleteStrukturPK(struktur.getIdStruktur());
            closeAll();
            return "struktur_list";
        }else{
            isHalamanOpened = true;
            idHalaman = id;
            state = "detail";
            return "designer";
        }
        
    }
    
    public String openDesainer(int id){
        isHalamanOpened = true;
//        idStruktur = id;
//        ArrayList<Halaman> halamans = backendBrain.fetchHalamansFK(idStruktur);
//        idHalaman = halamans.get(0).getIdHalaman();
        idHalaman = id;
        
        state = "detail";
        return "designer";
    }

    public String openDesainerAll(Integer hs, Integer he) {
        hstart = hs;
        hend = he;
        state = "detail";
        return "alldesigner";
    }

    public String manageJabatan(int id) {
        isJabatanOpened = true;
        idJabatan = id;
        state = "detail";
        return "manage";
    }

    public String manageJabatanParent(int id) {
        isJabatanOpened = true;
        idJabatan = id;
        state = "detail";
        return "parent";
    }

    public String managePenggunaan(int id) {
        isPenggunaanOpened = true;
        idPenggunaan = id;
        state = "detail";
        return "manage";
    }

    public String managePenggunaanChild(int id) {
        isPenggunaanOpened = true;
        idPenggunaan = id;
        state = "detail";
        return "penggunaan";
    }

    public String managePenggunaanParent(int id) {
        isPenggunaanOpened = true;
        idPenggunaan = id;
        state = "detail";
        return "parent";
    }

    public String managePelatihan(int id) {
        idPelatihan = id;
        state = "detail";
        return "manage";
    }

    public String managePertukaran(int id) {
        idPertukaran = id;
        state = "detail";
        return "manage";
    }

    public String managePelatihanChild(int id) {
        idPelatihan = id;
        state = "detail";
        return "pelatihan";
    }

    public String managePertukaranChild(int id) {
        idPertukaran = id;
        state = "detail";
        return "pertukaran";
    }

    public String manageTki(int id) {
        idTki = id;
        state = "detail";
        return "manage";
    }

    public String manageTka(int id) {
        idTka = id;
        state = "detail";
        return "manage";
    }

    public void beranda() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/welcome/index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pengajuanRPTK() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/rptk/pengajuan/list_pengajuan.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void tambahPengajuanRPTK() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/rptk/pengajuan/add.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void uploadSuratRPTK() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/rptk/pengajuan/upload_surat.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pengajuanIMTA() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/imta/pengajuan/list_pengajuan.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void tambahPengajuanIMTA() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/imta/pengajuan/add.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void editorPejabat() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/rptk/perubahanposisi/index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void epoTka() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/epo/tka.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void epoTki() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/epo/tki.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changePassword() {
        closeAll();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        FacesContext ctx = FacesContext.getCurrentInstance();
        String phase2 = ctx.getExternalContext().getInitParameter("phase2");
        try {
            context.redirect(phase2 + "/modules/pengaturan/akun.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(GlobalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String listKkks() {
        closeAll();
        state = "detail";
        return "kkks_list";
    }

    public String listUserkkks() {
        closeAll();
        state = "detail";
        return "userkkks_list";
    }

    public String listUseradmin() {
        closeAll();
        state = "detail";
        return "useradmin_list";
    }

    public String listRptk() {
        closeAll();
        return "rptk_list";
    }

    public String listNota() {
        closeAll();
        return "nota_list";
    }

    public String listStruktur() {
        closeAll();
        return "struktur_list";
    }

    public String listJabatan() {
        closeAll();
        return "jabatan_list";
    }

    public String listPenggunaan() {
        closeAll();
        return "penggunaan_list";
    }

    public String listPelatihan() {
        closeAll();
        return "pelatihan_list";
    }

    public String listPertukaran() {
        closeAll();
        return "pertukaran_list";
    }

    public String listLampiran() {
        closeAll();
        return "lampiran_list";
    }

    public String listTimesharing() {
        closeAll();
        return "timesharing_list";
    }

    public String listTki() {
        closeAll();
        return "tki_list";
    }

    public String listTka() {
        closeAll();
        return "tka_list";
    }

    public String demografi() {
        closeAll();
        return "demografi";
    }

    public String newKkks() {
        idKkks = -1;
        state = "new";
        return "manage";
    }

    public String newUserkkks() {
        idUserkkks = -1;
        state = "new";
        return "manage";
    }

    public String newRptk() {
        idRptk = -1;
        state = "new";
        return "manage";
    }

    public String newJabatan() {
        isJabatanOpened = true;
        idJabatan = -1;
        state = "new";
        return "manage";
    }

    public String newPenggunaan() {
        isPenggunaanOpened = true;
        idPenggunaan = -1;
        state = "new";
        return "manage";
    }

    public String newPelatihan() {
        idPelatihan = -1;
        state = "new";
        return "manage";
    }

    public String newPertukaran() {
        idPertukaran = -1;
        state = "new";
        return "manage";
    }

    public String newTki() {
        idTki = -1;
        state = "new";
        return "manage";
    }

    public String newTka() {
        idTka = -1;
        state = "new";
        return "manage";
    }

    public String parentPenggunaan(int id) {
        isPenggunaanOpened = true;
        idPenggunaan = id;
        state = "detail";
        return "parent";
    }

    public String openEditor() {
        return "edit";
    }

    public String discardThis(int id) {
        if (id > -1) {
            return "return";
        } else {
            return "list";
        }
    }

    public void revisiRptk(Rptk rptk) {
        this.ketSalin = "rev";
        this.salinRptk = rptk;
    }

    public void perpanjanganRptk(Rptk rptk) {
        this.ketSalin = "per";
        this.salinRptk = rptk;
    }

    public void openKkksPicker() {
        RequestContext.getCurrentInstance().openDialog("dialog/kkksPicker", pickerOptions, null);
    }

    public void pickKkks(SelectEvent event) {
        Kkks kkksPick = (Kkks) event.getObject();
        if (kkksPick != null) {
            this.filterKkksDefault = kkksPick.getIdKkks();
            this.filterNamaKkks = kkksPick.getNamaKkks();
            this.filterRptkDefault = -1;
            this.filterNamaRptk = "Pilih RPTK";
            rptkDefault = null;
        }
    }

    public void openRptkPicker() {
        RequestContext.getCurrentInstance().openDialog("dialog/rptkPicker", pickerOptions, null);
    }

    public void pickRptk(SelectEvent event) {
        Rptk rptkPick = (Rptk) event.getObject();
        if (rptkPick != null) {
            this.filterRptkDefault = rptkPick.getIdRptk();
            this.filterNamaRptk = "Tahun " + rptkPick.getTahun1() + " versi " + rptkPick.getVersi();
            rptkDefault = rptkPick;
        }

    }

    public void closeL1() {
        rptkDefault.setStLampiran1("Terkunci");
        Rptk rptk2 = backendBrain.fetchRptkPK(rptkDefault.getIdRptk());
        backendBrain.updateRptkPK(rptk2);
    }

    public void closeL2() {
        rptkDefault.setStLampiran2("Terkunci");
        Rptk rptk2 = backendBrain.fetchRptkPK(rptkDefault.getIdRptk());
        backendBrain.updateRptkPK(rptk2);
    }

    public void closeL3() {
        if (!"Terkunci".equals(rptkDefault.getStLampiran5())) {
            showMessage("Lampiran 5 harus dikunci dahulu");
        } else {
            rptkDefault.setStLampiran3("Terkunci");
            backendBrain.updateRptkPK(rptkDefault);
        }
    }

    public void closeL4() {
        rptkDefault.setStLampiran4("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void closeL5() {
        rptkDefault.setStLampiran5("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void closeL6() {
        rptkDefault.setStLampiran6("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void closeL7() {
        rptkDefault.setStLampiran7("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void closeL8() {
        rptkDefault.setStLampiran8("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void closeL9() {
        rptkDefault.setStLampiran9("Terkunci");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL1() {
        rptkDefault.setStLampiran1("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL2() {
        rptkDefault.setStLampiran2("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL3() {
        rptkDefault.setStLampiran3("Dalam Pengerjaan");
        rptkDefault.setStLampiran5("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL4() {
        rptkDefault.setStLampiran4("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL5() {
        rptkDefault.setStLampiran3("Dalam Pengerjaan");
        rptkDefault.setStLampiran5("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL6() {
        rptkDefault.setStLampiran6("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL7() {
        rptkDefault.setStLampiran7("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL8() {
        rptkDefault.setStLampiran8("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void openL9() {
        rptkDefault.setStLampiran9("Dalam Pengerjaan");
        backendBrain.updateRptkPK(rptkDefault);
    }

    public void stateDetail(ArrayList<String> formDone) {
        if (formDone.isEmpty()) {
            state = "detail";
        } else {
            undone = formDone;
            RequestContext.getCurrentInstance().openDialog("dialog/formUndone", pickerOptions, null);
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdUserkkks() {
        return idUserkkks;
    }

    public void setIdUserkkks(int idUserkkks) {
        this.idUserkkks = idUserkkks;
    }

    public int getIdKkks() {
        return idKkks;
    }

    public void setIdKkks(int idKkks) {
        this.idKkks = idKkks;
    }

    public int getIdRptk() {
        System.out.println("DEBUG IdRptk : " + idRptk);
        return idRptk;
    }

    public void setIdRptk(int idRptk) {
        this.idRptk = idRptk;
    }

    public int getIdRptkDefault() {
        return idRptkDefault;
    }

    public void setIdRptkDefault(int idRptkDefault) {
        this.idRptkDefault = idRptkDefault;
    }

    public int getIdStruktur() {
        return idStruktur;
    }

    public void setIdStruktur(int idStruktur) {
        this.idStruktur = idStruktur;
    }

    public int getIdHalaman() {
        return idHalaman;
    }

    public void setIdHalaman(int idHalaman) {
        this.idHalaman = idHalaman;
    }

    public int getIdJabatan() {
        return idJabatan;
    }

    public void setIdJabatan(int idJabatan) {
        this.idJabatan = idJabatan;
    }

    public int getIdPenggunaan() {
        return idPenggunaan;
    }

    public void setIdPenggunaan(int idPenggunaan) {
        this.idPenggunaan = idPenggunaan;
    }

    public int getIdPelatihan() {
        return idPelatihan;
    }

    public int getFilterKkks() {
        return filterKkks;
    }

    public void setFilterKkks(int filterKkks) {
        if (this.role.equals("skk")) {
            this.filterKkks = filterKkks;
        }
    }

    public int getFilterKkksDefault() {
        return filterKkksDefault;
    }

    public void setFilterKkksDefault(int filterKkksDefault) {
        this.filterKkksDefault = filterKkksDefault;
    }

    public int getFilterRptk() {
        return filterRptk;
    }

    public void setFilterRptk(int filterRptk) {
        this.filterRptk = filterRptk;
    }

    public int getFilterRptkDefault() {
        return filterRptkDefault;
    }

    public void setFilterRptkDefault(int filterRptkDefault) {
        this.filterRptkDefault = filterRptkDefault;
    }

    public int getFilterStruktur() {
        return filterStruktur;
    }

    public void setFilterStruktur(int filterStruktur) {
        this.filterStruktur = filterStruktur;
    }

    public int getFilterHalaman() {
        return filterHalaman;
    }

    public void setFilterHalaman(int filterHalaman) {
        this.filterHalaman = filterHalaman;
    }

    public int getFilterJabatan() {
        return filterJabatan;
    }

    public String getFilterNamaKkks() {
        return filterNamaKkks;
    }

    public void setFilterNamaKkks(String filterNamaKkks) {
        this.filterNamaKkks = filterNamaKkks;
    }

    public String getFilterNamaRptk() {
        return filterNamaRptk;
    }

    public void setFilterNamaRptk(String filterNamaRptk) {
        this.filterNamaRptk = filterNamaRptk;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isIsHalamanOpened() {
        return isHalamanOpened;
    }

    public void setIsHalamanOpened(boolean isHalamanOpened) {
        this.isHalamanOpened = isHalamanOpened;
    }

    public boolean isIsJabatanOpened() {
        return isJabatanOpened;
    }

    public void setIsJabatanOpened(boolean isJabatanOpened) {
        this.isJabatanOpened = isJabatanOpened;
    }

    public boolean isIsPenggunaanOpened() {
        return isPenggunaanOpened;
    }

    public void setIsPenggunaanOpened(boolean isPenggunaanOpened) {
        this.isPenggunaanOpened = isPenggunaanOpened;
    }

    public void setFilterJabatan(int filterJabatan) {
        this.filterJabatan = filterJabatan;
    }

    public void setIdPelatihan(int idPelatihan) {
        this.idPelatihan = idPelatihan;
    }

    public int getIdPertukaran() {
        return idPertukaran;
    }

    public void setIdPertukaran(int idPertukaran) {
        this.idPertukaran = idPertukaran;
    }

    public int getIdTka() {
        return idTka;
    }

    public void setIdTka(int idTka) {
        this.idTka = idTka;
    }

    public int getIdTki() {
        return idTki;
    }

    public void setIdTki(int idTki) {
        this.idTki = idTki;
    }

    public Rptk getSalinRptk() {
        return salinRptk;
    }

    public void setSalinRptk(Rptk salinRptk) {
        this.salinRptk = salinRptk;
    }

    public String getKetSalin() {
        return ketSalin;
    }

    public void setKetSalin(String ketSalin) {
        this.ketSalin = ketSalin;
    }

    public Rptk getRptkDefault() {
        return rptkDefault;
    }

    public void setRptkDefault(Rptk rptkDefault) {
        this.rptkDefault = rptkDefault;
    }

    public ArrayList<String> getUndone() {
        return undone;
    }

    public void setUndone(ArrayList<String> undone) {
        this.undone = undone;
    }

    public String roleSkk() {
        this.role = "skk";
        this.idKkks = -1;
        this.filterKkks = -1;
        this.filterNamaKkks = "Pilih KKKS";
        filterRptkDefault = -1;//untuk di filter main view gunakan default
        filterNamaRptk = "Pilih RPTK";
        return "role";
    }

    public String roleKkks() {
        this.role = "kkks";
        this.idKkks = 1;
        this.filterKkks = 1;
        this.filterNamaKkks = "Pilih KKKS";
        filterRptkDefault = -1;//untuk di filter main view gunakan default
        filterNamaRptk = "Pilih RPTK";
        return "role";
    }

    public void setRole(boolean iskkks, Integer idkkks) {
        System.out.println("ENTER ROLE : " + iskkks + " " + idkkks);
        sessionbumper = true;
        if (iskkks && idkkks != null) {
            this.role = "kkks";
            this.idKkks = idkkks;
            this.filterKkks = idkkks;
            this.filterKkksDefault = idkkks;
            this.filterNamaKkks = "Pilih KKKS";
        } else {
            System.out.println("ROLE ADMIN SKK");
            this.role = "skk";
        }
    }

    public void checksession() {
        ExternalContext externalContext = FacesContext.getCurrentInstance()
                .getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        ServletContext sharedContext = request.getSession().getServletContext().getContext("/rptk");
        String stage = FacesContext.getCurrentInstance().getApplication().getProjectStage().toString();

        if (stage.equals("Production")) {
            HashMap<String, Object> sessionObject = (HashMap<String, Object>) sharedContext.getAttribute(request.getSession().getId());
            if (sessionObject == null || sessionObject.get("token") == null) {
                System.out.println("logged out");
                FacesContext fc = FacesContext.getCurrentInstance();
                ExternalContext ec = fc.getExternalContext();
                try {
                    ec.redirect("../login.xhtml");
                } catch (IOException ex) {

                }
            }
        } else {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            if (sessionMap == null || sessionMap.get("token") == null) {
                System.out.println("logged out");
                FacesContext fc = FacesContext.getCurrentInstance();
                ExternalContext ec = fc.getExternalContext();
                try {
                    ec.redirect("../login.xhtml");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void checkuser() {
        ExternalContext externalContext = FacesContext.getCurrentInstance()
                .getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        ServletContext sharedContext = request.getSession().getServletContext().getContext("/rptk");
        String stage = FacesContext.getCurrentInstance().getApplication().getProjectStage().toString();

        if (stage.equals("Production")) {
            HashMap<String, Object> sessionObject = (HashMap<String, Object>) sharedContext.getAttribute(request.getSession().getId());
            List<String> roles = (List<String>) sessionObject.get("roles");
            if (!roles.contains("ROLE_KKKS") && !roles.contains("ROLE_SKK")) {
                System.out.println("logged out");
                FacesContext fc = FacesContext.getCurrentInstance();
                ExternalContext ec = fc.getExternalContext();
                try {
                    ec.redirect("../login.xhtml");
                } catch (IOException ex) {

                }
            }
        } else {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            List<String> roles = (List<String>) sessionMap.get("roles");
            if (!roles.contains("ROLE_KKKS") && !roles.contains("ROLE_SKK")) {
                System.out.println("logged out");
                FacesContext fc = FacesContext.getCurrentInstance();
                ExternalContext ec = fc.getExternalContext();
                try {
                    ec.redirect("../login.xhtml");
                } catch (IOException ex) {

                }
            }
        }
    }

    public int getTmpIdHalaman() {
        return tmpIdHalaman;
    }

    public void setTmpIdHalaman(int tmpIdHalaman) {
        this.tmpIdHalaman = tmpIdHalaman;
    }

    public StreamedContent getLogokkks() {
        System.out.println("logokkks = " + logokkks.getName());
        return logokkks;
    }

    public void setLogokkks(StreamedContent logokkks) {
        this.logokkks = logokkks;
    }

    public boolean isSessionbumper() {
        return sessionbumper;
    }

    public void setSessionbumper(boolean sessionbumper) {
        this.sessionbumper = sessionbumper;
    }

    public Integer getHstart() {
        return hstart;
    }

    public void setHstart(Integer hstart) {
        this.hstart = hstart;
    }

    public Integer getHend() {
        return hend;
    }

    public void setHend(Integer hend) {
        this.hend = hend;
    }

    public void setRptkDefaultToLatest(int idKkks) {
        this.filterRptkDefault = backendBrain.getLatestRptkId(idKkks);
        this.filterNamaRptk = backendBrain.getLatestRptkName(idKkks);
    }

    public void configRptk(int idRptk) {
        Rptk rptkPick = backendBrain.fetchRptkPK(idRptk);
        if (rptkPick != null) {
            this.filterRptkDefault = rptkPick.getIdRptk();
            this.filterNamaRptk = "Tahun " + rptkPick.getTahun1() + " versi " + rptkPick.getVersi();
            rptkDefault = rptkPick;
        }
    }

    public String backToRptk(int id) {
        idRptk = id;
        state = "detail";
        ketSalin = "det";
        return "rptk";
    }

    public String getUrlLogoKkks() {
        this.urlLogoKkks = backendBrain.getURLLogoKkks(this.filterKkksDefault);
        
        System.out.println("url logo: " + urlLogoKkks);
        
        if (!"placeholder".equals(this.urlLogoKkks)) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            String grailsurl
                    = ctx.getExternalContext().getInitParameter("backendurl");
            this.urlLogoKkks = grailsurl + this.urlLogoKkks;
        }
        return urlLogoKkks;
    }

}
