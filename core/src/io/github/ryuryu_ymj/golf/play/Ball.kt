package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.body
import ktx.box2d.circle

const val BALL_SIZE = 0.05f
const val BALL_DENSITY = 20f
const val NORMAL_DAMPING = 0.1f

class Ball(asset: AssetManager, world: World, centerX: Float, bottomY: Float) : Actor() {
    val body: Body
    private val texture = asset.get<Texture>("image/ball.png")
    private var lastSpeed = 0f
    private var decelerateCount = 0
    private var contactCount = 0
    private var isFirstContact = true

    init {
        setSize(BALL_SIZE, BALL_SIZE)
        setOrigin(width / 2, height / 2)
        setPosition(centerX - originX, bottomY)
        body = world.body {
            circle(radius = width / 2) {
                density = BALL_DENSITY
                restitution = 0.7f
                friction = 0.5f
            }
            type = BodyDef.BodyType.DynamicBody
            linearDamping = NORMAL_DAMPING
                        position.set(x + originX, y + originY)
        }
                world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                if (body == contact.fixtureA.body ||
                    body == contact.fixtureB.body
                ) {
                    contactCount++
                    isFirstContact = false
                }
            }

            override fun preSolve(contact: Contact, oldManifold: Manifold) {
            }

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {
            }

            override fun endContact(contact: Contact) {
                if (body == contact.fixtureA.body ||
                    body == contact.fixtureB.body
                ) {
                    contactCount--
                }
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)
        body.position.let { setPosition(it.x - originX, it.y - originY) }
        rotation = body.angle * MathUtils.radiansToDegrees

        if (contactCount > 0 && !isFirstContact) {
            val k = 0.1f
            body.applyForceToCenter(
                -body.linearVelocity.x * k,
                -body.linearVelocity.y * k,
                false
            )
        }

        val speed = body.linearVelocity.len()
                if (speed > lastSpeed) {
            if (decelerateCount > 0) {
                decelerateCount = 0
                            }
        } else if (speed < 0.1f) {
            decelerateCount++
                        if (decelerateCount > 60) {
                body.isAwake = false
                isFirstContact = true
            }
        }
        lastSpeed = speed
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        /*batch.draw(
            texture, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation,
            0, 0, texture.width, texture.height, false, false
        )*/
    }
}