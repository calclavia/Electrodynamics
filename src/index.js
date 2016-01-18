function main() {
  console.info("Launched");

  //Loads the resource pack
  RenderManager.loadAsset("edx");

  RenderManager.textureRegistry().add("edx:blocks/iron_block");

  var turbineModel = RenderManager.modelRegistry().add("edx:metal_turbine.b3d");
  turbineModel.bindObjects(["LargeMetalBlade", "LargeMetalHub"]);
  turbineModel.bindTexture("iron_block", "edx:blocks/iron_block");

  BlockManager.createBlock('test', 'Test Block');
  EventManager.addEvent(
    function event_handler(event) {
      var ent = EntityManager.instantiate(event.world, "turbine");
      ent.x = event.pos.getX() + 0.5;
      ent.y = event.pos.getY() + 1;
      ent.z = event.pos.getZ() + 0.5;
    },
    'RIGHT_CLICK_BLOCK',
    'Test Block'
  );

  EntityManager.create("turbine")
    .then(function(entity) {
      entity.sizeX = 1;
      entity.sizeY = 1;

      entity.frame = 0;

      //Update event
      entity
        .on("Update")
        .bind(function(e) {
          entity.frame = (entity.frame + 2) % 60;
        });

      //Renderer event
      entity
        .on("Render")
        .bind(function(evt) {
          evt.pipeline
            .then(function(renderer) {
              turbineModel.setFrame(entity.frame);
              renderer.addChild(turbineModel);
            });
        });
      return entity;
    });
}
