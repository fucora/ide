<template>
  <div
    class="app-repository"
    :class="{'show' : $route.path == '/','repository-fullscreen':repository.fullscreen}"
  >
    <div
      class="repository-box"
      v-loading="repository.loading"
      :element-loading-text="repository.loading_msg"
    >
      <div class="repository-top"></div>
      <div class="repository-left">
        <Project :repository="repository"></Project>
      </div>
      <div
        class="repository-center"
        :class="{'show' : $route.path == '/','repository-center-fullscreen':repository.fullscreencenter}"
      >
        <div class="repository-screen-control-toolbar">
          <a
            class="repository-screen-control coos-icon coos-icon-fullscreen"
            @click="source.fullScreenCenter()"
            v-if="!repository.fullscreencenter"
            title="撑满屏幕"
          ></a>
          <a
            class="repository-screen-control repository-screen-control-active coos-icon coos-icon-fullscreen-exit"
            @click="source.fullScreenExitCenter()"
            v-if="repository.fullscreencenter"
            title="还原屏幕"
          ></a>
        </div>
        <Tabs :repository="repository"></Tabs>
      </div>
      <div class="repository-contextmenu">
        <Contextmenu ref="context-menu" :contextmenu="repository.contextmenu"></Contextmenu>
      </div>

      <Starter :repository="repository"></Starter>

      <GitRemote ref="git-remote"></GitRemote>
      <GitPlus ref="git-plus"></GitPlus>
      <GitPush ref="git-push"></GitPush>
      <GitRevert ref="git-revert"></GitRevert>
      <StarterForm ref="starter-form"></StarterForm>
      <BranchCreate ref="branch-create"></BranchCreate>
      <UploadForm ref="upload-form"></UploadForm>
      <ProjectForm ref="project-form"></ProjectForm>
    </div>
  </div>
</template>

<script>
import Project from "@/views/repository/Project";
import Tabs from "@/views/repository/Tabs";
import Starter from "@/views/repository/Starter";
import Contextmenu from "@/views/components/Contextmenu";

import GitRemote from "@/views/repository/components/git/GitRemote";
import GitPlus from "@/views/repository/components/git/GitPlus";
import GitPush from "@/views/repository/components/git/GitPush";
import GitRevert from "@/views/repository/components/git/GitRevert";
import StarterForm from "@/views/repository/components/starter/StarterForm";
import BranchCreate from "@/views/repository/components/branch/BranchCreate";
import UploadForm from "@/views/repository/components/UploadForm";
import ProjectForm from "@/views/repository/components/Project";

export default {
  props: ["repository"],
  components: {
    Project,
    Tabs,
    Starter,
    Contextmenu,
    GitRemote,
    GitPlus,
    GitPush,
    GitRevert,
    StarterForm,
    BranchCreate,
    UploadForm,
    ProjectForm
  },
  data() {
    return { source: source };
  },
  beforeMount() {},
  watch: {},
  methods: {},
  mounted() {
    source.gitRemoteForm = this.$refs["git-remote"];
    source.gitPlusForm = this.$refs["git-plus"];
    source.gitPushForm = this.$refs["git-push"];
    source.gitRevertForm = this.$refs["git-revert"];
    source.starterForm = this.$refs["starter-form"];
    source.branchCreateForm = this.$refs["branch-create"];
    source.uploadForm = this.$refs["upload-form"];
    source.projectForm = this.$refs["project-form"];

    this.repository.contextmenu.callShow = this.$refs["context-menu"].callShow;
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style  >
.app-repository {
  display: none;
  background-color: #ddd;
  position: absolute;
  top: 105px;
  left: 0px;
  bottom: 0px;
  width: 100%;
}
.app-repository.show {
  display: block;
}
.app-repository.repository-fullscreen {
  top: 0px;
}
.repository-screen-control-toolbar {
  position: absolute;
  top: 0px;
  right: 0px;
  z-index: 1;
  height: 25px;
  line-height: 25px;
}
.repository-screen-control {
  cursor: pointer;
  background: #efefef;
  color: #afafaf;
  padding: 4px;
  font-size: 17px;
}
.repository-screen-control:hover,
.repository-screen-control-active {
  background: #989898;
  color: #fff;
}
.repository-box {
  position: relative;
  background-color: #ddd;
  box-shadow: 0px 0px 10px #ddd;
  width: 100%;
  height: 100%;
}
.repository-box .repository-top {
  position: absolute;
  top: 5px;
  left: 0px;
  right: 0px;
  height: 35px;
  background-color: #fff;
  display: none;
}
.repository-box .repository-left {
  position: absolute;
  top: 0px;
  left: 0px;
  bottom: 0px;
  width: 310px;
  background-color: #fff;
  box-shadow: 0px 0px 10px #ddd;
}
.repository-box .repository-left .repository-left-content {
  position: absolute;
  top: 30px;
  left: 0px;
  bottom: 0px;
  right: 0px;
}
.repository-box .repository-center {
  position: absolute;
  top: 0px;
  left: 310px;
  bottom: 0px;
  right: 0px;
  background-color: #fff;
  box-shadow: 0px 0px 10px #ddd;
}
.repository-box .repository-center.repository-center-fullscreen {
  top: -105px;
  left: 0px;
}
</style>
